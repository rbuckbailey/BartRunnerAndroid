# Keep app-specific rules here as reflection or shrinker warnings are found.

# Android Auto / Cars App Library Keep Rules
-keep class in.izyum.bart.car.** { *; }
-keep public class * extends androidx.car.app.CarAppService
-keep public class * extends androidx.car.app.Session
-keep public class * extends androidx.car.app.Screen


