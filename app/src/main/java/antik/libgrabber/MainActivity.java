package antik.libgrabber;

/*
 * Created by aantik
 * 3/27/2026 8:28 PM
 *
 *   ⋆    ႔ ႔
 *     ᠸ^ ^ ⸝⸝
 *       |、˜〵
 *      じしˍ,)⁐̤ᐷ
 *
 * Fox Mode 🍺
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private EditText in1, in2;
    private Button lever, browse, browse2;
    private TextView stone;
    private ProgressBar proccce;

    private String[] S = {"Processing <--> ", "Invalid file", "Input required", "Allow file access", "Permission Required", "Allow", "Cancel"};


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);


        setContentView(R.layout.activity_main);

        Toolbar t = findViewById(R.id.tool);
        setSupportActionBar(t);

        in1 = findViewById(R.id.in1);
        in2 = findViewById(R.id.in2);
        lever = findViewById(R.id.dum);
        browse = findViewById(R.id.browse);
        browse2 = findViewById(R.id.browse2);
        stone = findViewById(R.id.sta);
        proccce = findViewById(R.id.proc);

        lever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Ouverture du selecteur...", Toast.LENGTH_SHORT).show();
                openFilePicker();
            }
        });

        browse2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Ouverture du selecteur...", Toast.LENGTH_SHORT).show();
                openFolderPicker();
            }
        });

        permissionsAppe();
    }

    //-- open native .so file picker
    private void openFilePicker() {

        if (!hasStoragePermission()) {
            Toast.makeText(this, S[3], Toast.LENGTH_SHORT).show();
            permissionsAppe();
            return;
        }

        File root = getRootStorageDir();

        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = root;
        properties.error_dir = root;
        properties.offset = root;
        properties.extensions = new String[]{"so"};

        try {
            FilePickerDialog dialog = new FilePickerDialog(MainActivity.this, properties);
            dialog.setTitle("Select .so file");

            dialog.setDialogSelectionListener(new DialogSelectionListener() {
                @Override
                public void onSelectedFilePaths(String[] files) {
                    if (files != null && files.length > 0) {
                        in1.setText(files[0]);
                    }
                }
            });

            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Picker error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //-- pick output folder, auto-name the .hpp from the chosen .so
    private void openFolderPicker() {

        if (!hasStoragePermission()) {
            Toast.makeText(this, S[3], Toast.LENGTH_SHORT).show();
            permissionsAppe();
            return;
        }

        File root = getRootStorageDir();

        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = root;
        properties.error_dir = root;
        properties.offset = root;

        try {
            FilePickerDialog dialog = new FilePickerDialog(MainActivity.this, properties);
            dialog.setTitle("Select output folder");

            dialog.setDialogSelectionListener(new DialogSelectionListener() {
                @Override
                public void onSelectedFilePaths(String[] files) {
                    if (files != null && files.length > 0) {

                        String folder = files[0];
                        String input = in1.getText().toString().trim();
                        String baseName = "output";

                        if (!TextUtils.isEmpty(input)) {
                            File inFile = new File(input);
                            String name = inFile.getName();
                            int dot = name.lastIndexOf('.');
                            baseName = (dot > 0) ? name.substring(0, dot) : name;
                        }

                        File outFile = new File(folder, baseName + ".hpp");
                        in2.setText(outFile.getAbsolutePath());
                    }
                }
            });

            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Picker error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //-- resolve the real shared-storage root, bypassing Environment.getExternalStorageDirectory()
    //-- which on some Android 12+ ROMs (notably MIUI) resolves through a symlink into /mnt
    //-- instead of the real /storage/emulated/0 path.
    private File getRootStorageDir() {

        File candidate = new File("/storage/emulated/0");
        if (candidate.exists() && candidate.isDirectory() && candidate.canRead()) {
            return candidate;
        }

        try {
            File resolved = Environment.getExternalStorageDirectory().getCanonicalFile();
            if (resolved.exists() && resolved.isDirectory()) {
                return resolved;
            }
        } catch (java.io.IOException ignored) {
        }

        return Environment.getExternalStorageDirectory();
    }

    //-- deep write
    private void permissionsAppe() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {alu();}
            } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 100);
            }
        }
    }

    private boolean hasStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void alu() {

        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);

        b.setTitle(S[4]);
        b.setMessage(S[3]);

        b.setPositiveButton(S[5], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int w) {
                Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
                d.dismiss();
            }
        });

        b.setNegativeButton(S[6], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int w) {
            d.dismiss();
            }
        });
        AlertDialog dialog = b.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int r, String[] p, int[] g) {
        super.onRequestPermissionsResult(r, p, g);
    }
    private void start() {

        String input = in1.getText().toString().trim();
        String out = in2.getText().toString().trim();

        if (TextUtils.isEmpty(input)) {
        stone.setText(S[2]);
        return;
        }

        if (TextUtils.isEmpty(out)) {
        out = input.replace(".so", ".hpp");
        in2.setText(out);
        }

        File inFile = new File(input);
        if (!inFile.exists()) {
        stone.setText(S[1]);
        return;
        }

        File outFile = new File(out);
        if (outFile.getParentFile() != null) {
        outFile.getParentFile().mkdirs();
        }

        proccce.setVisibility(View.VISIBLE);
        lever.setEnabled(false);
        stone.setText(S[0]);

        final String finalOut = out;

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {

                final String result = lib.dump(input, finalOut);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        proccce.setVisibility(View.GONE);
                        lever.setEnabled(true);
                        stone.setText(result);
                    }
                });
            }
        });

        th.start();
    }
}
