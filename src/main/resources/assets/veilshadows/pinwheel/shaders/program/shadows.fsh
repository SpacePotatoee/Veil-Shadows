#include veil:material
#include veil:deferred_utils
#include veilshadows:shadows

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D DepthSampler;
uniform sampler2D NormalSampler;
uniform sampler2D NoiseTex;
uniform sampler2D ShadowSampler;
uniform sampler2D HandDepth;

uniform vec2 Velocity;
uniform ivec2 resolution;
uniform float GameTime;

uniform mat4 viewMatrix;
uniform mat4 orthographMatrix;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler0, texCoord);
    vec4 normal = texture(NormalSampler, texCoord);
    float depth = texture(DepthSampler, texCoord).r;
    float depth2 = texture(DiffuseDepthSampler, texCoord).r;
    float handDepth = texture(HandDepth, texCoord).r;
    vec3 viewPos = viewPosFromDepth(depth, texCoord);

    if(depth2 < 1.0 && handDepth >= 1.0){
        color = getShadow(color, texCoord, viewPos, normal, viewMatrix, orthographMatrix, NoiseTex, ShadowSampler, depth2);
    }
    fragColor = color;
}