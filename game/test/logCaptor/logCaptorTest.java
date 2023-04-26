package logCaptor;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import ecs.components.HealthComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import java.util.logging.Logger;
import nl.altindag.log.LogCaptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

public class logCaptorTest {
    HealthComponent healthComponent;
    Entity testEntity;

    Logger healthLogger;

    @Before
    public void setup() {
        testEntity = Mockito.mock(Entity.class);
        healthComponent = new HealthComponent(testEntity);
    }

    @Test
    public void logger_test() {
        LogCaptor logCaptor = LogCaptor.forClass(HealthComponent.class);

        Entity enemyEntity = Mockito.mock(Entity.class);
        Damage damage = new Damage(5, DamageType.PHYSICAL, enemyEntity);
        healthComponent.receiveHit(damage);
        healthComponent.getDamage(DamageType.PHYSICAL);

        String expectedLog =
                healthComponent.getClass().getSimpleName()
                        + " processed damage for entity '"
                        + testEntity.getClass().getSimpleName()
                        + "': "
                        + damage.damageAmount();

        // anstelle von verify()
        assertEquals(1, logCaptor.getLogs().size());

        // anstelle von assertTrue() oder assertEquals()
        // dynamisches Einbinden der Variablen in LogMessage
        assertEquals(expectedLog, logCaptor.getDebugLogs().get(0));
    }
}
