package logCaptor;

import static org.junit.Assert.assertEquals;

import ecs.components.HealthComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import java.util.logging.Logger;
import logging.CaptorHandler;
import logging.CustomLogLevel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LogCaptorTest {
    HealthComponent healthComponent;
    Entity testEntity;
    CaptorHandler testHandler = new CaptorHandler();

    @Before
    public void setup() {
        testEntity = Mockito.mock(Entity.class);
        healthComponent = new HealthComponent(testEntity);
    }

    @Test
    public void logger_test() {
        Logger captorLogger = Logger.getLogger("");
        captorLogger.setLevel(CustomLogLevel.ALL);
        testHandler.addLogger(Logger.getLogger(healthComponent.getClass().getName()));
        captorLogger.addHandler(testHandler);

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
        assertEquals(1, testHandler.getLogList().size());

        // anstelle von assertTrue() oder assertEquals()
        // dynamisches Einbinden der Variablen in LogMessage

        // TODO dieses Testverhalten kann eine IndexOutOfBoundsException werfen, falls es keine Logs
        // mit diesem Level gibt
        // Testcase muss dann abgesichert werden
        if (testHandler.getLogsByLevel(CustomLogLevel.DEBUG).size() > 0) {
            assertEquals(
                    expectedLog,
                    testHandler.getLogsByLevel(CustomLogLevel.DEBUG).get(0).getMessage());
        }
    }
}
