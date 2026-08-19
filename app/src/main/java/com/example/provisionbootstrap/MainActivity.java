package com.example.provisionbootstrap;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Minimal bootstrapper whose only job is to trigger
 * ACTION_PROVISION_MANAGED_DEVICE via startActivityForResult().
 *
 * Why this exists: firing the same intent via `adb shell am start`
 * fails on some Android builds with "Calling package is null. Was
 * startActivityForResult used to start this activity?" -- Managed
 * Provisioning explicitly requires a real calling app, which a raw
 * shell command can't provide. This Activity IS that real calling app.
 *
 * Usage:
 *   adb install -r provision-bootstrap.apk
 *   adb shell am start -n com.example.provisionbootstrap/.MainActivity
 *
 * The device still needs to be in a clean state (no accounts, no
 * existing device/profile owner) before running this, same as before.
 */
public class MainActivity extends Activity {

    private static final String TAG = "ProvisionBootstrap";
    private static final int REQUEST_CODE_PROVISION = 1;

    // Same values used in the earlier adb attempts -- change these if
    // your DPC package/receiver or checksum differ.
    private static final String DPC_COMPONENT_PACKAGE = "com.google.android.apps.work.clouddpc";
    private static final String DPC_COMPONENT_CLASS =
            "com.google.android.apps.work.clouddpc.receivers.CloudDeviceAdminReceiver";
    private static final String DPC_SIGNATURE_CHECKSUM =
            "I5YvS0O5hXY46mb01BlRjq4oJJGs2kuUcHvVkAPEXlg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE);
        intent.putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                new ComponentName(DPC_COMPONENT_PACKAGE, DPC_COMPONENT_CLASS));
        intent.putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM,
                DPC_SIGNATURE_CHECKSUM);

        Log.i(TAG, "Firing ACTION_PROVISION_MANAGED_DEVICE via startActivityForResult");

        try {
            startActivityForResult(intent, REQUEST_CODE_PROVISION);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start provisioning intent", e);
            Toast.makeText(this, "Provisioning failed to start: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PROVISION) {
            Log.i(TAG, "Provisioning flow returned resultCode=" + resultCode
                    + " (RESULT_OK=" + RESULT_OK + ")");
            Toast.makeText(this,
                    resultCode == RESULT_OK ? "Provisioning completed" : "Provisioning did not complete (code " + resultCode + ")",
                    Toast.LENGTH_LONG).show();
        }
        finish();
    }
}
