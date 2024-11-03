# Veil Shadows

[![experimental](http://badges.github.io/stability-badges/dist/experimental.svg)](http://github.com/badges/stability-badges)

Just adds simple shadow mapping using veil

The shadows CAN follow the sun if you so choose. However there is a bug associated with it that has yet to be fixed.

If you want to edit the angle of the shadows, go to the [createShadowModelView()](https://github.com/SpacePotatoee/Veil-Shadows/blob/master/src/main/java/com/sp/VeilShadowsClient.java#L70) method.

If you want to edit the strength/smoothness of the shadows, change the respective definitions in the [Shadow Include](https://github.com/SpacePotatoee/Veil-Shadows/blob/master/src/main/resources/assets/veilshadows/pinwheel/shaders/include/shadows.glsl#L4) shaders.
