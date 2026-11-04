package com.github.olympus.proteus.client.ui;

import com.mojang.blaze3d.platform.InputConstants;

public record RawKey(int code, InputConstants.Key key, boolean escape) {
}
