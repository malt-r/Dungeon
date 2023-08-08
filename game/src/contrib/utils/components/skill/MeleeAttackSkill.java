package contrib.utils.components.skill;

import contrib.utils.components.draw.AdditionalAnimations;
import contrib.utils.components.health.DamageType;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimations;

import java.util.function.Supplier;

public class MeleeAttackSkill extends DamageProjectile {

    private static final String MELEE_TEXTURE = "character/blue_knight/attack";
    private static final float PROJECTILE_SPEED = 0.0f;
    private static final int DAMAGE_AMOUNT = 1;
    private static final DamageType DAMAGE_TYPE = DamageType.PHYSICAL;
    private static final Point HITBOX_SIZE = new Point(1, 1);
    private static final float PROJECTILE_RANGE = 1f;

    /**
     * Create a {@link DamageProjectile} that looks like a fireball and will cause fire damage.
     *
     * @param targetSelection A function used to select the point where the projectile should fly
     *     to.
     * @see DamageProjectile
     */
    public MeleeAttackSkill(Supplier<Point> targetSelection) {
        super(
                MELEE_TEXTURE,
                PROJECTILE_SPEED,
                DAMAGE_AMOUNT,
                DAMAGE_TYPE,
                HITBOX_SIZE,
                targetSelection,
                PROJECTILE_RANGE);
    }

    @Override
    public void accept(Entity entity) {
        super.accept(entity);
        DrawComponent entityDraw =
                entity.fetch(DrawComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(entity, DrawComponent.class));

        PositionComponent entityPosComp =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(entity, DrawComponent.class));

        Point entityPos = entityPosComp.position();
        Point pointerPos = SkillTools.cursorPositionAsPoint();

        if (pointerPos.x < entityPos.x) {
            entityDraw.currentAnimation(AdditionalAnimations.FIGHT_LEFT);
        }
        else {
            entityDraw.currentAnimation(AdditionalAnimations.FIGHT_RIGHT);
        }
    }
}
