package com.samsung.health.hrdatatransfer;

import com.google.android.gms.wearable.MessageClient;
import com.samsung.health.hrdatatransfer.data.MessageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class MainModule_ProvideMessageRepositoryFactory implements Factory<MessageRepository> {
  private final MainModule module;

  private final Provider<MessageClient> messageClientProvider;

  public MainModule_ProvideMessageRepositoryFactory(MainModule module,
      Provider<MessageClient> messageClientProvider) {
    this.module = module;
    this.messageClientProvider = messageClientProvider;
  }

  @Override
  public MessageRepository get() {
    return provideMessageRepository(module, messageClientProvider.get());
  }

  public static MainModule_ProvideMessageRepositoryFactory create(MainModule module,
      Provider<MessageClient> messageClientProvider) {
    return new MainModule_ProvideMessageRepositoryFactory(module, messageClientProvider);
  }

  public static MessageRepository provideMessageRepository(MainModule instance,
      MessageClient messageClient) {
    return Preconditions.checkNotNullFromProvides(instance.provideMessageRepository(messageClient));
  }
}
