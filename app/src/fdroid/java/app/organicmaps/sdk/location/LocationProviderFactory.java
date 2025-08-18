// app/src/fdroid/java/app/organicmaps/sdk/location/LocationProviderFactory.java
package app.organicmaps.sdk.location;

import android.content.Context;
import androidx.annotation.NonNull;
import app.organicmaps.sdk.util.Config;
import app.organicmaps.sdk.util.log.Logger;

public class LocationProviderFactory
{
  private static final String TAG = LocationProviderFactory.class.getSimpleName();

  // Di FDroid tidak pernah pakai Google Play Services.
  public static boolean isGoogleLocationAvailable(@NonNull Context context)
  {
    return false;
  }

  public static BaseLocationProvider getProvider(@NonNull Context context,
                                                 @NonNull BaseLocationProvider.Listener listener)
  {
    Logger.d(TAG, "FDroid flavor: force native provider");
    return new AndroidNativeProvider(context, listener);
  }
}
