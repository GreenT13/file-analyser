package nl.ricoapon.fileanalyser.internal;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReflectionUtilTest {
    private static class CannotInstantiate {
        public CannotInstantiate(String input) {
        }
    }

    @Test
    void exceptionIsThrownWhenConstructorDoesNotExist() {
        assertThrows(FileAnalyserConfigurationException.class, () -> ReflectionUtil.instantiate(CannotInstantiate.class));
    }

    public static class CanInstantiate {
    }

    @Test
    void happyFlow() {
        assertThat(ReflectionUtil.instantiate(CanInstantiate.class), instanceOf(CanInstantiate.class));
    }
}
