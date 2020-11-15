package nl.ricoapon.fileanalyser.analyser;

import nl.ricoapon.fileanalyser.internal.FileAnalyserConfigurationException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotations that can be placed on implementations of {@link BlockAnalyser} to configure the order of analysers.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface BlockAnalyserOrder {
    /**
     * Indicates that this block analyser must be called after the defined block analyser are called. This relation is
     * transitively calculated. {@link FileAnalyserConfigurationException} is thrown if a cyclic relation occurs.
     */
    Class<? extends BlockAnalyser>[] after() default {};
}
