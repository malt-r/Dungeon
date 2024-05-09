package dslinterop.dsltypeproperties;

import core.Entity;
import dsl.annotation.DSLExtensionMethod;
import dsl.annotation.DSLTypeProperty;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionMethod;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import task.game.components.TaskContentComponent;
import task.game.content.QuestItem;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Class, which stores dsl-extensions (e.g. {@link IDSLExtensionProperty} or {@link
 * IDSLExtensionMethod}) for {@link QuestItem}s
 */
public class QuestItemExtension {
  // private ctor, because this class should not be instantiated
  private QuestItemExtension() {}

  /**
   * {@link IDSLExtensionProperty} extension to access the {@link TaskContentComponent} of a {@link
   * QuestItem} instance. Not settable.
   */
  @DSLTypeProperty(
      name = "task_content_component",
      extendedType = QuestItem.class,
      isSettable = false)
  public static class TaskContentComponentProperty
      implements IDSLExtensionProperty<QuestItem, TaskContentComponent> {
    public static TaskContentComponentProperty instance = new TaskContentComponentProperty();

    private TaskContentComponentProperty() {}

    @Override
    public void set(QuestItem instance, TaskContentComponent valueToSet) { }

    @Override
    public TaskContentComponent get(QuestItem instance) {
      return instance.taskContentComponent();
    }
  }

  @DSLExtensionMethod(
    name = "use",
    extendedType = QuestItem.class)
  public static class UseMethod
    implements IDSLExtensionMethod<QuestItem, Void> {
    public static UseMethod instance = new UseMethod();

    private UseMethod() {}

    @Override
    public Void call(QuestItem instance, List<Object> params) {
      Entity paramEntity = (Entity)params.get(0);
      instance.use(paramEntity);
      return null;
    }

    @Override
    public List<Type> getParameterTypes() {
      return List.of(Entity.class);
    }
  }
}
