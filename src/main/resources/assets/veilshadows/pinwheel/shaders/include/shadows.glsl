#include veil:deferred_utils
#include veil:camera

#define SHADOW_SAMPLES 3

#define SHADOW_STRENGTH 0.75

#define VOL_LIGHT

vec3 distort(in vec3 shadowPosition) {
    const float bias0 = 0.95;
    const float bias1 = 1.0 - bias0;

    float factorDistance = length(shadowPosition.xy);

    float distortFactor = factorDistance * bias0 + bias1;

    return shadowPosition * vec3(vec2(1.0 / distortFactor), 0.2);
}

mat2 randRotMat(vec2 coord, sampler2D NoiseTex){
    float randomAngle = texture(NoiseTex, coord * 20.0).r * 100.0;
    float cosTheta = cos(randomAngle);
    float sinTheta = sin(randomAngle);
    return mat2(cosTheta, -sinTheta, sinTheta, cosTheta) / 2048;
}

vec3 getShadowCoords(vec4 normal, vec3 viewPos, mat4 viewMatrix, mat4 orthographMatrix){
    vec3 playerSpace = viewToPlayerSpace(viewPos);
    vec3 adjustedPlayerSpace = playerSpace + 0.01 * viewToWorldSpaceDirection(normal.rgb) * length(viewPos);
    vec3 shadowViewPos = (viewMatrix * vec4(adjustedPlayerSpace, 1.0)).xyz;
    vec4 homogenousPos = orthographMatrix * vec4(shadowViewPos, 1.0);
    vec3 shadowNdcPos = homogenousPos.xyz / homogenousPos.w;
    vec3 distortedNdcSpace = distort(shadowNdcPos);
    vec3 shadowScreenSpace = distortedNdcSpace * 0.5 + 0.5;
    shadowScreenSpace.z = shadowScreenSpace.z - 0.0001;

    return shadowScreenSpace;
}

vec3 getShadowCoords(vec3 playerSpace, mat4 viewMatrix, mat4 orthographMatrix){
    vec3 shadowViewPos = (viewMatrix * vec4(playerSpace, 1.0)).xyz;
    vec4 homogenousPos = orthographMatrix * vec4(shadowViewPos, 1.0);
    vec3 shadowNdcPos = homogenousPos.xyz / homogenousPos.w;
    vec3 distortedNdcSpace = distort(shadowNdcPos);
    vec3 shadowScreenSpace = distortedNdcSpace * 0.5 + 0.5;
    shadowScreenSpace.z = shadowScreenSpace.z - 0.0001;

    return shadowScreenSpace;
}

vec4 getShadow(vec4 incolor, vec2 texCoord, vec3 viewPos, vec4 normal, mat4 viewMatrix, mat4 orthographMatrix, sampler2D NoiseTex, sampler2D ShadowSampler, float depth2){
    float worldDepth = length(viewPos);
    vec4 color = incolor;

    //SHADOWS
    vec3 LIGHT_COLOR = vec3(1);
    color.rgb = pow(color.rgb, vec3(2.2));

    vec3 lightangle = (viewMatrix * vec4(0.0, 0.0, 1.0, 0.0)).xyz;
    lightangle.xy = -lightangle.xy;

    float lightDir = dot(normalize(lightangle), viewToWorldSpaceDirection(normal.rgb));

    if (lightDir > 0.0){
        vec3 shadowScreenSpace = getShadowCoords(normal, viewPos, viewMatrix, orthographMatrix);

        float shadowDepth = shadowScreenSpace.z;

        float shadowSum = 0.0;
        mat2 randRotation = randRotMat(texCoord, NoiseTex);
        for (int x = - SHADOW_SAMPLES; x <= SHADOW_SAMPLES; x++){
            for (int y = - SHADOW_SAMPLES; y <= SHADOW_SAMPLES; y++){
                vec2 offset = randRotation * vec2(x, y);
                float shadowSampler = texture(ShadowSampler, shadowScreenSpace.xy + offset).r;

                if (shadowDepth < shadowSampler){
                    shadowSum += 1.0;
                }
            }
        }

        shadowSum /= pow(2.0 * SHADOW_SAMPLES + 1.0, 2.0);
        color.rgb *= (clamp(shadowSum, 1.0 - SHADOW_STRENGTH, 1.0)) * LIGHT_COLOR;
//        color.rgb = texture(ShadowSampler, shadowScreenSpace.xy + offset).rgb;
    }
    else{
        color.rgb = (color.rgb * (1.0 - SHADOW_STRENGTH)) * LIGHT_COLOR;
    }


    //////////////////////////////////////////////////////////////////////////
    #ifdef VOL_LIGHT
    //VOLUMETRIC LIGHT
    vec3 ro = VeilCamera.CameraPosition;
    vec3 rd = normalize(viewToPlayerSpace(viewPos));
    float dist = 0.0;
    float brightness = 0.0;

    //raymarch
    if(depth2 < 1.0){
        for (int i = 0; i <= 500; i++){
            vec3 rp = ro + rd * dist;
            dist += 0.03;

            if (dist > worldDepth){
                break;
            }

            vec3 playerSpace = rp - ro;
            vec3 shadowScreenSpace = getShadowCoords(playerSpace, viewMatrix, orthographMatrix);
            float shadowDepth = shadowScreenSpace.z;
            float shadowSampler = texture(ShadowSampler, shadowScreenSpace.xy).r;


            if (shadowDepth < shadowSampler){
                brightness += 0.0001;
            }

            if (brightness >= 1.5){
                brightness = 1.5;
                break;
            }

        }
    }else{
        brightness = 1.5;
    }

    color.rgb += (brightness * LIGHT_COLOR);
    #endif

    color.rgb = pow(color.rgb, vec3(1 / 2.2));

    return color;
}