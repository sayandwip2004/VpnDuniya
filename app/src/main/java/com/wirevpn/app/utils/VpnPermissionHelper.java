package com.wirevpn.app.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;

/**
 * Utility class to handle VPN permission requests.
 * Android requires explicit user consent before a VPN tunnel can be created.
 */
public class VpnPermissionHelper {

    public static final int VPN_PERMISSION_REQUEST_CODE = 100;

    /**
     * Returns true if the app already has VPN permission (no dialog needed).
     * Returns false if the app needs to request permission (caller should
     * start the returned Intent for result with VPN_PERMISSION_REQUEST_CODE).
     *
     * Usage:
     *   Intent intent = VpnPermissionHelper.prepareVpn(this);
     *   if (intent != null) {
     *       startActivityForResult(intent, VPN_PERMISSION_REQUEST_CODE);
     *   } else {
     *       // Already have permission – connect directly
     *   }
     */
    public static Intent prepareVpn(Activity activity) {
        return VpnService.prepare(activity);
    }

    /**
     * Call this in onActivityResult to check if permission was granted.
     */
    public static boolean isPermissionGranted(int requestCode, int resultCode) {
        return requestCode == VPN_PERMISSION_REQUEST_CODE
                && resultCode == Activity.RESULT_OK;
    }
}
