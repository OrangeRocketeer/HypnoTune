package com.samsung.health.hrdatatransfer;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;

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
public final class MainModule_ProvideApplicationCoroutineScopeFactory implements Factory<CoroutineScope> {
  private final MainModule module;

  public MainModule_ProvideApplicationCoroutineScopeFactory(MainModule module) {
    this.module = module;
  }

  @Override
  public CoroutineScope get() {
    return provideApplicationCoroutineScope(module);
  }

  public static MainModule_ProvideApplicationCoroutineScopeFactory create(MainModule module) {
    return new MainModule_ProvideApplicationCoroutineScopeFactory(module);
  }

  public static CoroutineScope provideApplicationCoroutineScope(MainModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideApplicationCoroutineScope());
  }
}
