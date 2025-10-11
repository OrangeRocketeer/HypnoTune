package com.samsung.health.mobile.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class MusicProfileManager_Factory implements Factory<MusicProfileManager> {
  private final Provider<StageConfigManager> stageConfigManagerProvider;

  public MusicProfileManager_Factory(Provider<StageConfigManager> stageConfigManagerProvider) {
    this.stageConfigManagerProvider = stageConfigManagerProvider;
  }

  @Override
  public MusicProfileManager get() {
    return newInstance(stageConfigManagerProvider.get());
  }

  public static MusicProfileManager_Factory create(
      Provider<StageConfigManager> stageConfigManagerProvider) {
    return new MusicProfileManager_Factory(stageConfigManagerProvider);
  }

  public static MusicProfileManager newInstance(StageConfigManager stageConfigManager) {
    return new MusicProfileManager(stageConfigManager);
  }
}
