package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.programmanalyzer.Relatable;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public interface IType extends Relatable {
  enum Kind {
    Basic,
    Aggregate,
    AggregateAdapted,
    FunctionType,
    SetType,
    ListType,
    MapType,
    EnumType
  }

  /**
   * Getter for the type name
   *
   * @return the name of the type
   */
  String getName();

  /** */
  Kind getTypeKind();
}
