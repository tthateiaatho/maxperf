# MaxPerf (Fabric, Minecraft 26.2)

- Applies performance-tuned video settings once on first launch
  (delete `config/maxperf.applied` to re-apply).
- Permanent client-side Night Vision, no icon/particles, toggle with **N**.

## Build
Requires **JDK 25** and **Gradle 9.5.1**.

    gradle wrapper --gradle-version 9.5.1     # once, generates ./gradlew
    ./gradlew build

Jar appears in build/libs/. Needs Fabric Loader 0.19.3+ and Fabric API for 26.2.

## Notes on 26.x
- Minecraft is unobfuscated: Loom plugin is `net.fabricmc.fabric-loom`, no mappings, `implementation` not `modImplementation`.
- Names are Mojang's (KeyMapping, MobEffectInstance, Identifier...), and Fabric's KeyBindingHelper became KeyMappingHelper.
- If the compiler complains about an `Options` accessor, open the decompiled game jar and check its current name.

## For actual max FPS, install alongside
Sodium, Lithium, FerriteCore, EntityCulling, ImmediatelyFast, ModernFix (use the 26.2 builds).
