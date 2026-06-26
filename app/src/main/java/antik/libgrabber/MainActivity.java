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

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private EditText in1, in2;
    private Button lever, browse, browse2;
    private TextView stone;
    private ProgressBar proccce;

    //-- URIs chosen through the Android system pickers (SAF)
    private Uri inputUri;
    private Uri outputUri;

    private ActivityResultLauncher<String[]> pickSoLauncher;
    private ActivityResultLauncher<String> createHppLauncher;

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

        //-- system file picker for the .so input
        pickSoLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                new androidx.activity.result.ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(@Nullable Uri uri) {
                        if (uri != null) {
                            inputUri = uri;
                            in1.setText(queryDisplayName(uri));
                            if (TextUtils.isEmpty(in2.getText())) {
                                stone.setText("Fichier selectionne. Choisis ou enregistrer.");
                            }
                        }
                    }
                });

        //-- system "create document" picker for the .hpp output
        createHppLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/x-c++hdr"),
                new androidx.activity.result.ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(@Nullable Uri uri) {
                        if (uri != null) {
                            outputUri = uri;
                            in2.setText(queryDisplayName(uri));
                        }
                    }
                });

        lever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //-- accept any file; many ROMs don't map .so to a MIME type
                pickSoLauncher.launch(new String[]{"*/*"});
            }
        });

        browse2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String suggested = suggestedOutputName();
                createHppLauncher.launch(suggested);
            }
        });
    }

    //-- propose <inputbasename>.hpp as the default output file name
    private String suggestedOutputName() {
        String base = "output";
        if (inputUri != null) {
            String name = queryDisplayName(inputUri);
            if (!TextUtils.isEmpty(name)) {
                int dot = name.lastIndexOf('.');
                base = (dot > 0) ? name.substring(0, dot) : name;
            }
        }
        return base + ".hpp";
    }

    //-- resolve a human-readable file name from a content:// uri
    private String queryDisplayName(Uri uri) {
        String result = uri.getLastPathSegment();
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    String n = c.getString(idx);
                    if (!TextUtils.isEmpty(n)) result = n;
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private void start() {

        if (inputUri == null) {
            stone.setText("Choisis d'abord un fichier .so");
            return;
        }

        proccce.setVisibility(View.VISIBLE);
        lever.setEnabled(false);
        stone.setText("Processing <--> ");

        final Uri fInput = inputUri;
        final Uri fOutput = outputUri;

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;
                try {
                    //-- copy the chosen .so into app cache to obtain a real path
                    File inCache = new File(getCacheDir(), "input.so");
                    copyUriToFile(fInput, inCache);

                    File outCache = new File(getCacheDir(), "output.hpp");

                    final String result = lib.dump(inCache.getAbsolutePath(), outCache.getAbsolutePath());

                    //-- push the produced header back to the location the user picked
                    if (outCache.exists() && fOutput != null) {
                        copyFileToUri(outCache, fOutput);
                        message = result + "\nEnregistre : " + queryDisplayName(fOutput);
                    } else if (outCache.exists()) {
                        //-- no output location chosen: keep it in the app folder
                        File ext = new File(getExternalFilesDir(null), suggestedOutputName());
                        copyFile(outCache, ext);
                        message = result + "\nEnregistre : " + ext.getAbsolutePath();
                    } else {
                        message = result;
                    }
                } catch (Throwable e) {
                    message = "Erreur : " + e.getMessage();
                }

                final String finalMessage = message;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        proccce.setVisibility(View.GONE);
                        lever.setEnabled(true);
                        stone.setText(finalMessage);
                    }
                });
            }
        });

        th.start();
    }

    //-- stream helpers -------------------------------------------------------

    private void copyUriToFile(Uri uri, File dest) throws Exception {
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(dest)) {
            if (in == null) throw new Exception("Lecture impossible du fichier choisi");
            pump(in, out);
        }
    }

    private void copyFileToUri(File src, Uri uri) throws Exception {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
            if (out == null) throw new Exception("Ecriture impossible vers la destination");
            pump(in, out);
        }
    }

    private void copyFile(File src, File dst) throws Exception {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            pump(in, out);
        }
    }

    private void pump(InputStream in, OutputStream out) throws Exception {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) {
            out.write(buf, 0, n);
        }
        out.flush();
    }
}
