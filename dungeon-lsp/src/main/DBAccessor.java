package main;

import dsl.parser.ast.ASTErrorNode;
import dsl.parser.ast.Node;
import dsl.parser.ast.SourceFileReference;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.lsp4j.Position;
import org.neo4j.ogm.session.Session;

public class DBAccessor {
  private final Session session;

  public DBAccessor(Session session) {
    this.session = session;
  }

  public Map<String, Object> resolveContextToNearestAstNode(Position position) {
    var result =
        session.query(
            """
        // find nearest ASTNode (furthest to right)
        match (n:AstNode)-[]-(nearestSfr:SourceFileReference) where nearestSfr.startLine = $startline and nearestSfr.endColumn < $endcolumn and n.hasErrorRecord = false
        and not exists {(n)<-[:CHILD_OF]-(:AstNode)}
        match (n:AstNode)-[:CHILD_OF]->(parent:AstNode)
        match (parent)-[]-(parentSfr:SourceFileReference)
        optional match (n)-[:CHILD_OF*]->(typeRestrictingContext:AstNode) where typeRestrictingContext.type IN ["PropertyDefinition","ExpressionList", "ReturnStmt", "Assignment"]
        return n, parent, nearestSfr, parentSfr, typeRestrictingContext, typeRestrictingContext.type as restrictionType
        //return n, parent, nearestSfr, parentSfr
        order by nearestSfr.startColumn desc
        limit 1
        """,
            Map.of("startline", position.getLine(), "endcolumn", position.getCharacter()));
    var iter = result.iterator();
    if (iter.hasNext()) {
      var map = iter.next();
      var typeRestrictionContextObj = map.get("typeRestrictingContext");
      if (typeRestrictionContextObj == null) {
        map.put("typeRestrictingContext", Node.NONE);
        map.put("restrictionType", "");
      }
      return map;
    } else {
      return Map.of(
          "n",
          Node.NONE,
          "parent",
          Node.NONE,
          "nearestSfr",
          SourceFileReference.NULL,
          "parentSfr",
          SourceFileReference.NULL,
          "typeRestrictingContext",
          Node.NONE,
          "restrictionType",
          "");
    }
  }

  public Map<String, Object> resolveContext(Position position) {
    var result =
        session.query(
            """
        // resolve context v3
        match (n:AstNode)-[]-(nearestSfr:SourceFileReference) where nearestSfr.startLine = $startline and nearestSfr.endColumn < $endcolumn and n.hasErrorRecord = false and not exists {(n)<-[:CHILD_OF]-(:AstNode)}
        CALL{
        with n, nearestSfr
        // match closest scope, either referenced scopes symbol (in memberaccess) or created (in normal scope)
        match p=(n)-[:REFERENCES]-(sym:Symbol)-[:OF_TYPE]-(scope:ScopedSymbol)
        return scope,p
        UNION
        // match closest scope, either referenced scopes symbol (in memberaccess) or created (in normal scope)
        match p=(n)-[:CHILD_OF*]-(parent:AstNode)-[:CREATES]-(scope:IScope)
        return scope,p
        }
        return scope, n, nearestSfr,length(p)
        //order by length(p) desc
        Order by nearestSfr.endColumn desc, length(p)
        LIMIT 1
        """,
            Map.of("startline", position.getLine(), "endcolumn", position.getCharacter()));

    var iter = result.iterator();
    return iter.next();
  }

  // TODO: does not work as expected...
  public List<Symbol> getAllSymbolsInParentScopeLikeLhsType(Node memberAccessNode, String prefix) {
    var result =
        session.query(
            """
        call {
            match (na:AssignmentNode) where na.internalId=$internalId
            match (na)-[:PARENT_OF {idx:0}]->(lhsChild:AstNode)
            match (lhsChild)-[:REFERENCES]-(sym:Symbol)-[:OF_TYPE]-(lhsType:IType)
            return lhsChild, na, lhsType
            limit 1
        }

        call {
            with scope
            match l=(scope)
            return scope as _scope, 1 as len, l
            UNION
            with scope
            // collect parent scopes
            match l=((scope)-[:PARENT_SCOPE|IN_SCOPE*]->(parentScope:IScope))
            return parentScope as _scope, length(l) as len, l
        }

        match (symbol:Symbol)-[:IN_SCOPE]->(_scope) where
        exists {(symbol)-[:OF_TYPE]->(lhsType)} and
        symbol.name STARTS WITH $prefix
        return symbol, lhsType
  """,
            Map.of("internalId", memberAccessNode.getId(), "prefix", prefix));

    ArrayList<Symbol> symbols = new ArrayList<>();
    result.forEach(m -> symbols.add((Symbol) m.get("symbol")));
    return symbols;
  }

  public Map<String, Object> getSymbolAndTypeOfNode(Node prefixNode) {
    var result =
        session.query(
            """
          match (n:AstNode)-[:REFERENCES]->(sym:Symbol)-[:OF_TYPE]->(t:IType) where n.internalId=$internalId
          return n, sym, t
          """,
            Map.of("internalId", prefixNode.getId()));

    var iter = result.iterator();
    if (iter.hasNext()) {
      return iter.next();
    } else {
      return Map.of("n", Node.NONE, "sym", Symbol.NULL, "t", BuiltInType.noType);
    }
  }

  // Note: this filters out datatypes
  public List<RankedSymbol> getAllSymbolsOfTypeInScopeAndParentScopes(
      Node prefixNode, String typeRestriction, String prefix) {
    var result =
        session.query(
            """
          //get symbols in scope and parent scopes v2
          call{
            match (n:AstNode) where n.internalId=$internalId
            // match closest scope created (in normal scope)
            match p=(n)-[:CHILD_OF*]-(parent:AstNode)-[:CREATES]-(scope:IScope)
            return scope, p, parent
            order by length(p)
            limit 1
          }
          call {
            with scope
            match l=(scope)
            return scope as _scope, 1 as len, l
            UNION
            with scope
            // collect parent scopes
            match l=((scope)-[:PARENT_SCOPE|IN_SCOPE*]->(parentScope:IScope))
            return parentScope as _scope, length(l) as len, l
          }
          //return distinct scope, _scope, len,l

          //UNWIND scopes as scopeToFocus
          match (symbol:Symbol)-[:IN_SCOPE]-(_scope) where symbol.name starts with $prefix and not symbol:IType
          match (symbol)-[:OF_TYPE]->(type:IType)
          optional match (symbol)-[:OF_TYPE]->(matchingType:IType) where matchingType.name=$typeRestriction
          return distinct symbol, type, matchingType
          """,
            Map.of(
                "internalId",
                prefixNode.getId(),
                "prefix",
                prefix,
                "typeRestriction",
                typeRestriction));

    ArrayList<RankedSymbol> symbols = new ArrayList<>();
    result.forEach(
        m -> {
          var matchingType = m.get("matchingType");
          int ranking = matchingType == null ? 0 : 1;
          var type = (IType) m.get("type");
          symbols.add(new RankedSymbol((Symbol) m.get("symbol"), type, ranking));
        });
    return symbols;
  }

  public List<RankedSymbol> getAllSymbolsInScopeAndParentScopes(
      Node prefixNode, String typeRestriction, String prefix) {
    var result =
        session.query(
            """
  //get symbols in scope and parent scopes v2
  call{
      match (n:AstNode) where n.internalId=$internalId
      // match closest scope created (in normal scope)
      match p=(n)-[:CHILD_OF*]-(parent:AstNode)-[:CREATES]-(scope:IScope)
      return scope, p, parent
      order by length(p)
      limit 1
  }
  call {
      with scope
      match l=(scope)
      return scope as _scope, 1 as len, l
      UNION
      with scope
      // collect parent scopes
      match l=((scope)-[:PARENT_SCOPE|IN_SCOPE*]->(parentScope:IScope))
      return parentScope as _scope, length(l) as len, l
  }
  //return distinct scope, _scope, len,l

  //UNWIND scopes as scopeToFocus
  match (symbol:Symbol)-[:IN_SCOPE]-(_scope) where symbol.name starts with $prefix
  match (symbol)-[:OF_TYPE]->(type:IType)
  optional match (symbol)-[:OF_TYPE]->(matchingType:IType) where matchingType.name=$typeRestriction
  return distinct symbol, type, matchingType
  """,
            Map.of(
                "internalId",
                prefixNode.getId(),
                "prefix",
                prefix,
                "typeRestriction",
                typeRestriction));

    ArrayList<RankedSymbol> symbols = new ArrayList<>();
    result.forEach(
        m -> {
          var matchingType = m.get("matchingType");
          int ranking = matchingType == null ? 0 : 1;
          var type = (IType) m.get("type");
          symbols.add(new RankedSymbol((Symbol) m.get("symbol"), type, ranking));
        });
    return symbols;
  }

  public List<RankedSymbol> getSymbolsInScopeOfIdentifier(Node identifier, String typeRestriction) {
    var result =
        session.query(
            """
            call {
                match (n:AstNode) where n.internalId=$internalId
                match (n)-[:REFERENCES]-(sym:Symbol)-[:OF_TYPE]-(scope:ScopedSymbol)
                return n, scope
                limit 1
            }
            match (symbol:Symbol)-[:IN_SCOPE]->(scope)
            match (symbol)-[:OF_TYPE]->(type:IType)
            optional match (symbol)-[:OF_TYPE]->(matchingType:IType) where matchingType.name=$typeRestriction
            return symbol, type, matchingType
            """,
            Map.of("internalId", identifier.getId(), "typeRestriction", typeRestriction));

    ArrayList<RankedSymbol> symbols = new ArrayList<>();
    result.forEach(
        m -> {
          var matchingType = m.get("matchingType");
          var type = (IType) m.get("type");
          int ranking = matchingType == null ? 0 : 1;
          symbols.add(new RankedSymbol((Symbol) m.get("symbol"), type, ranking));
        });
    return symbols;
  }

  public List<Long> getChildIdxsOfNode(Node node) {
    var result =
        session.query(
            """
          match (n:AstNode) where n.internalId=$internalId
          match (n)-[childEdge:PARENT_OF]->(child:AstNode)
          return child.internalId as childIdx, n order by childEdge.idx
        """,
            Map.of("internalId", node.getId()));

    ArrayList<Long> idxs = new ArrayList<>();
    result.forEach(m -> idxs.add((Long) m.get("childIdx")));
    return idxs;
  }

  public List<Long> getChildIdxsOfMemberAccess(Node memberAccesNode) {
    var result =
        session.query(
            """
      match (n:MemberAccessNode) where n.internalId=$internalId
      match (n)-[childEdge:PARENT_OF]->(child:AstNode)
      return child.internalId as childIdx, n order by childEdge.idx
    """,
            Map.of("internalId", memberAccesNode.getId()));

    ArrayList<Long> idxs = new ArrayList<>();
    result.forEach(m -> idxs.add((Long) m.get("childIdx")));
    return idxs;
  }

  public List<RankedSymbol> getSymbolsInScopeOfLhsIdentifierWithPrefix(
      Node memberAccessParentNode, String prefix, String typeRestriction) {

    var result =
        session.query(
            """
            call {
                match (n:MemberAccessNode) where n.internalId=$internalId
                match (n)-[:PARENT_OF {idx:0}]->(lhsChild:AstNode)
                match (lhsChild)-[:REFERENCES]-(sym:Symbol)-[:OF_TYPE]-(scope:ScopedSymbol)
                return lhsChild, n, scope
                limit 1
            }
            match (symbol:Symbol)-[:IN_SCOPE]->(scope) where symbol.name STARTS WITH $prefix
            match (symbol)-[:OF_TYPE]->(type:IType)
            optional match (symbol)-[:OF_TYPE]->(matchingType:IType) where matchingType.name=$typeRestriction
            return symbol, type, matchingType
            """,
            Map.of(
                "internalId",
                memberAccessParentNode.getId(),
                "prefix",
                prefix,
                "typeRestriction",
                typeRestriction));

    ArrayList<RankedSymbol> symbols = new ArrayList<>();
    result.forEach(
        m -> {
          var matchingType = m.get("matchingType");
          int ranking = matchingType == null ? 0 : 1;
          var type = (IType) m.get("type");
          symbols.add(new RankedSymbol((Symbol) m.get("symbol"), type, ranking));
        });
    return symbols;
  }

  public Map<String, Object> sparseContextResolving(Position position) {
    var result =
        session.query(
            """
    // find nearest ASTNode (furthest to right)
    match (n:AstNode)-[]-(nearestSfr:SourceFileReference) where nearestSfr.startLine = $startline and nearestSfr.endColumn < $endcolumn
    and not exists {(n)<-[:CHILD_OF]-(:AstNode)}
    optional match (n:AstNode)-[:CHILD_OF]->(parent:AstNode)
    optional match (parent)-[]-(parentSfr:SourceFileReference)
    optional match (n)-[:CHILD_OF*]->(typeRestrictingContext:AstNode) where typeRestrictingContext.type IN ["PropertyDefinition","ExpressionList", "ReturnStmt", "Assignment"]
    return n, parent, nearestSfr, parentSfr, typeRestrictingContext, typeRestrictingContext.type as restrictionType
    //return n, parent, nearestSfr, parentSfr
    order by nearestSfr.startColumn desc
    limit 1
    """,
            Map.of("startline", position.getLine(), "endcolumn", position.getCharacter()));
    var iter = result.iterator();
    if (iter.hasNext()) {
      var map = iter.next();
      var typeRestrictionContextObj = map.get("typeRestrictingContext");
      var matchedNode = map.get("n");
      var nearestSfr = map.get("nearestSfr");
      var parent = map.get("parent");
      var parentSfr = map.get("parentSfr");
      var restrictionType = map.get("restrictionType");
      String matchedText = "";
      if (matchedNode instanceof ASTErrorNode errorNode) {
        matchedText = errorNode.optionalMatchedText();
      }
      return Map.of(
          "n",
          matchedNode != null ? matchedNode : Node.NONE,
          "parent",
          parent != null ? parent : Node.NONE,
          "nearestSfr",
          nearestSfr != null ? nearestSfr : SourceFileReference.NULL,
          "parentSfr",
          parentSfr != null ? parentSfr : SourceFileReference.NULL,
          "typeRestrictionContext",
          typeRestrictionContextObj != null ? typeRestrictionContextObj : Node.NONE,
          "restrictionType",
          restrictionType != null ? restrictionType : "",
          "matchedText",
          matchedText);
    } else {
      return Map.of(
          "n",
          Node.NONE,
          "parent",
          Node.NONE,
          "nearestSfr",
          SourceFileReference.NULL,
          "parentSfr",
          SourceFileReference.NULL,
          "typeRestrictingContext",
          Node.NONE,
          "restrictionType",
          "");
    }
  }
}