package com.samsung.health.hrdatatransfer;

import android.content.Context;
import com.google.android.gms.wearable.MessageClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class MainModule_ProvideMessageClientFactory implements Factory<MessageClient> {
  private final MainModule module;

  private final Provider<Context> contextProvider;

  public MainModule_ProvideMessageClientFactory(MainModule module,
      Provider<Context> contextProvider) {
    this.module = module;
    this.contextProvider = contextProvider;
  }

  @Override
  public MessageClient get() {
    return provideMessageClient(module, contextProvider.get());
  }

  public static MainModule_ProvideMessageClientFactory create(MainModule module,
      Provider<Context> contextProvider) {
    return new MainModule_ProvideMessageClientFactory(module, contextProvider);
  }

  public static MessageClient provideMessageClient(MainModule instance, Context context) {
    return Preconditions.checkNotNullFromProvides(instance.provideMessageClient(context));
  }
}
