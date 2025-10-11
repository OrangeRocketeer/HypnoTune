package com.samsung.health.mobile.ml;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class MLModelInterface_Factory implements Factory<MLModelInterface> {
  @Override
  public MLModelInterface get() {
    return newInstance();
  }

  public static MLModelInterface_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MLModelInterface newInstance() {
    return new MLModelInterface();
  }

  private static final class InstanceHolder {
    static final MLModelInterface_Factory INSTANCE = new MLModelInterface_Factory();
  }
}
