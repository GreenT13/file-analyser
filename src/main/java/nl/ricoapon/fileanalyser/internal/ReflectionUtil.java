package nl.ricoapon.fileanalyser.internal;

import java.lang.reflect.InvocationTargetException;

/**
 * Class containing utility methods related to reflection.
 */
public class ReflectionUtil {
    private ReflectionUtil() {
        // This class should not be instantiated.
    }

    /**
     * @param clazz The class to instantiate an object of.
     * @param <C> The type of the object.
     * @return Instance of {@link C} created using the no-arg constructor.
     * @throws FileAnalyserConfigurationException If anything goes wrong.
     */
    public static <C> C instantiate(Class<C> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new FileAnalyserConfigurationException("The class " + clazz.getName() + " could not be constructed " +
                    "using the no-args constructor.", e);
        }
    }
}
