package contrib.utils.components.skill;

import contrib.utils.components.health.DamageType;
import core.utils.Point;

import java.util.function.Supplier;

public class MeleeAttackSkill extends DamageProjectile {

    private static final String MELEE_TEXTURE = "character/blue_knight/attack";
    private static final float PROJECTILE_SPEED = 0.0f;
    private static final int DAMAGE_AMOUNT = 1;
    private static final DamageType DAMAGE_TYPE = DamageType.PHYSICAL;
    private static final Point HITBOX_SIZE = new Point(1, 1);
    private static final float PROJECTILE_RANGE = 0f;

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
}
