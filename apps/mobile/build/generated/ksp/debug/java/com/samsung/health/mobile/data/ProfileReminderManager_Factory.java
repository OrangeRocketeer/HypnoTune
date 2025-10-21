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
public final class ProfileReminderManager_Factory implements Factory<ProfileReminderManager> {
  @Override
  public ProfileReminderManager get() {
    return newInstance();
  }

  public static ProfileReminderManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ProfileReminderManager newInstance() {
    return new ProfileReminderManager();
  }

  private static final class InstanceHolder {
    static final ProfileReminderManager_Factory INSTANCE = new ProfileReminderManager_Factory();
  }
}
