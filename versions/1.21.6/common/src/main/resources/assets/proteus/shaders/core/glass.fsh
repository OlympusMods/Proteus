#version 330
#extension GL_ARB_separate_shader_objects : enable

layout(std140) uniform GlassInfo {
    vec4 Shape;
    vec4 Optics;
    vec4 Lens;
    vec4 Light;
    vec4 Glow;
    vec4 Tint;
};

uniform sampler2D Sampler0;

layout(location = 0) in vec2 local;

layout(location = 0) out vec4 fragColor;

const int TAPS = 20;
const float GOLDEN = 2.39996323;

float roundedBox(vec2 p, vec2 size, float radius) {
    vec2 q = abs(p) - size + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

vec2 roundedBoxNormal(vec2 p, vec2 size, float radius) {
    vec2 q = abs(p) - size + radius;
    vec2 n;
    if (q.x > 0.0 && q.y > 0.0) {
        n = normalize(q);
    } else if (q.x > q.y) {
        n = vec2(1.0, 0.0);
    } else {
        n = vec2(0.0, 1.0);
    }
    return n * vec2(p.x < 0.0 ? -1.0 : 1.0, p.y < 0.0 ? -1.0 : 1.0);
}

vec3 fetch(vec2 px) {
    px = clamp(px, vec2(0.5), Lens.zw - 0.5);
    return texture(Sampler0, px / Lens.zw).rgb;
}

vec3 sampleSplit(vec2 px, vec2 shift, float spread) {
    if (spread <= 0.0) {
        return fetch(px + shift);
    }
    return vec3(
        fetch(px + shift * (1.0 - spread)).r,
        fetch(px + shift).g,
        fetch(px + shift * (1.0 + spread)).b);
}

vec3 frosted(vec2 px, vec2 shift, float spread, float radius, vec2 along) {
    if (radius < 0.5 && dot(along, along) < 0.25) {
        return sampleSplit(px, shift, spread);
    }
    vec3 sum = vec3(0.0);
    float total = 0.0;
    for (int i = 0; i < TAPS; i++) {
        float f = (float(i) + 0.5) / float(TAPS);
        float a = float(i) * GOLDEN;
        float s = fract(float(i) * 0.618034) * 2.0 - 1.0;
        vec2 o = vec2(cos(a), sin(a)) * sqrt(f) * radius + along * s;
        float weight = exp(-2.0 * f) * (1.0 - 0.5 * s * s);
        sum += sampleSplit(px + o, shift, spread) * weight;
        total += weight;
    }
    return sum / total;
}

void main() {
    mat2 toLocal = mat2(dFdx(local), dFdy(local));
    float det = determinant(toLocal);
    if (abs(det) < 1.0e-8) {
        discard;
    }
    mat2 toScreen = inverse(toLocal);
    float scale = sqrt(abs(determinant(toScreen)));

    float radius = min(Shape.z, min(Shape.x, Shape.y));
    float d = roundedBox(local, Shape.xy, radius);
    float coverage = clamp(0.5 - d * scale, 0.0, 1.0);
    if (coverage <= 0.0) {
        discard;
    }

    vec2 n = roundedBoxNormal(local, Shape.xy, radius);
    float depth = max(min(Optics.y, min(Shape.x, Shape.y)), 0.001);
    float edge = clamp(1.0 + d / depth, 0.0, 1.0);
    float bend = 1.0 - sqrt(max(0.0, 1.0 - edge * edge));

    float band = pow(edge, 1.5);
    vec2 tangent = vec2(-n.y, n.x);
    vec2 across = local / Shape.xy;
    float shortest = min(Shape.x, Shape.y);
    float zoom = sign(Lens.x) * min(abs(Lens.x), shortest * 0.5);
    float smear = min(Lens.y, shortest);
    vec2 lensShift = -n * bend * Optics.x - across * band * zoom;
    vec2 shift = toScreen * lensShift;
    vec2 along = toScreen * (tangent * band * smear);
    vec2 here = gl_FragCoord.xy;
    vec3 color = frosted(here, shift, Optics.w * max(bend, band * 0.3), Optics.z * scale, along);

    float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(vec3(luma), color, Light.z);
    color = mix(color, Tint.rgb, Tint.a);

    vec2 toward = vec2(sin(Light.y), -cos(Light.y));
    float facing = dot(n, toward);
    float lit = max(facing, 0.0);
    float shade = max(-facing, 0.0);
    float line = 1.0 - smoothstep(0.0, 1.75 / scale, -d);
    float sheen = edge * edge * edge;
    float specular = line * (0.25 + 0.75 * pow(lit, 1.5)) + line * 0.45 * pow(shade, 2.0);
    color += Light.x * (specular * 0.85 + sheen * lit * 0.22);
    color -= Light.x * sheen * shade * 0.10 * color;
    float top = clamp(0.5 - local.y / (2.0 * Shape.y), 0.0, 1.0);
    color += Light.x * 0.035 * top;

    if (Glow.z > 0.0) {
        float dist = length(local - Glow.xy) / Glow.w;
        color += Glow.z * 0.28 * exp(-dist * dist * 2.0) * (0.6 + 0.4 * sheen);
    }

    fragColor = vec4(clamp(color, 0.0, 1.0), coverage * Light.w);
}
