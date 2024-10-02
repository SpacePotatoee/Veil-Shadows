package com.sp.util;

import com.mojang.blaze3d.shaders.Uniform;

public interface UniformStuff {

    Uniform orthoMatrix = null;
    Uniform viewMatrix = null;
    Uniform lightAngle = null;

    public Uniform getOrthoMatrix();
    public Uniform getViewMatrix();
    public Uniform getLightAngle();

}
