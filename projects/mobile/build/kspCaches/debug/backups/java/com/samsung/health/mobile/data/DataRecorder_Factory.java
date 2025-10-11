package com.samsung.health.mobile.data;

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
public final class DataRecorder_Factory implements Factory<DataRecorder> {
  @Override
  public DataRecorder get() {
    return newInstance();
  }

  public static DataRecorder_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DataRecorder newInstance() {
    return new DataRecorder();
  }

  private static final class InstanceHolder {
    static final DataRecorder_Factory INSTANCE = new DataRecorder_Factory();
  }
}
