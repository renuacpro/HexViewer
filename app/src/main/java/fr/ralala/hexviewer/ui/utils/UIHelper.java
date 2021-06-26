package fr.ralala.hexviewer.ui.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.utils.FileHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * UI Helper functions
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class UIHelper {

  /**
   * Sets the activity title.
   *
   * @param activity    Activity.
   * @param orientation Screen orientation.
   * @param addAppName  Adds the application name?
   * @param filename    The file name.
   */
  public static void setTitle(final Activity activity, int orientation, boolean addAppName, final String filename) {
    ApplicationCtx app = ApplicationCtx.getInstance();
    int length = 0;
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
      length = app.getAbbreviateLandscape();
    } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      length = app.getAbbreviatePortrait();
    }
    final String name = activity.getString(R.string.app_name);
    if (length != 0) {
      String title = "";
      if (addAppName) {
        title += name;
        if (filename != null)
          title += " - ";
      } else
        length += name.length();
      if (app.getHexChanged().get())
        title += "*";
      if (filename != null) {
        title += SysHelper.abbreviate(filename, length);
      }
      activity.setTitle(title);
    } else
      activity.setTitle(name);
  }

  /**
   * Opens the file picker in directory selection mode.
   *
   * @param activityResultLauncher Activity Result Launcher.
   */
  public static void openFilePickerInDirectorSelectionMode(final ActivityResultLauncher<Intent> activityResultLauncher) {
    activityResultLauncher.launch(FileHelper.prepareForOpenDirectory());
  }

  /**
   * Opens the file picker in file selection mode.
   *
   * @param c                      Android context.
   * @param activityResultLauncher Activity Result Launcher.
   * @param snackBarLayout         Layout used to attach the snackbar.
   */
  public static void openFilePickerInFileSelectionMode(final Context c, final ActivityResultLauncher<Intent> activityResultLauncher, final View snackBarLayout) {
    try {
      activityResultLauncher.launch(
          Intent.createChooser(FileHelper.prepareForOpenFile(), c.getString(R.string.select_file_to_open)));
    } catch (android.content.ActivityNotFoundException ex) {
      Snackbar customSnackBar = Snackbar.make(snackBarLayout, c.getString(R.string.error_no_file_manager), Snackbar.LENGTH_LONG);
      customSnackBar.setAction(c.getString(R.string.install), (v) -> {
        final String search = c.getString(R.string.file_manager_keyword);
        try {
          c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + search + "&c=apps")));
        } catch (android.content.ActivityNotFoundException ignore) {
          c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=" + search + "&c=apps")));
        }
      });
      customSnackBar.show();
    }
  }

  /**
   * Shake a view on error.
   *
   * @param owner   The owner view.
   * @param errText The error text.
   */
  public static void shakeError(EditText owner, String errText) {
    TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
    shake.setDuration(500);
    shake.setInterpolator(new CycleInterpolator(5));
    if (owner != null) {
      if (errText != null)
        owner.setError(errText);
      owner.clearAnimation();
      owner.startAnimation(shake);
    }
  }

  /**
   * Displays a confirm dialog.
   *
   * @param c       The Android context.
   * @param title   The dialog title.
   * @param message The dialog message.
   * @param yes     Listener used when the 'yes' button is clicked.
   */
  public static void showConfirmDialog(final Context c, String title,
                                       String message, final View.OnClickListener yes) {
    new AlertDialog.Builder(c)
        .setCancelable(false)
        .setIcon(R.mipmap.ic_launcher)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
          if (yes != null) yes.onClick(null);
        })
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
        }).show();
  }

  /**
   * Displays a toast.
   *
   * @param c       The Android context.
   * @param message The toast message.
   */
  public static void toast(final Context c, final String message) {
    /* Create a toast with the launcher icon */
    Toast toast = Toast.makeText(c, message, Toast.LENGTH_LONG);
    toast.show();
  }


  public interface DialogPositiveClick {
    void onClick(AlertDialog dialog, EditText editText, TextInputLayout editTextLayout);
  }

  /**
   * Creation of a dialog box with an edittext in it.
   *
   * @param c             The Android context.
   * @param title         Title of the dialog box.
   * @param defaultValue  Default value.
   * @param positiveClick Listener called when clicking on the validation button.
   * @return AlertDialog
   */
  @SuppressLint("InflateParams")
  public static AlertDialog createTextDialog(final Context c, String title, String defaultValue, DialogPositiveClick positiveClick) {
    AlertDialog.Builder builder = new AlertDialog.Builder(c);
    builder.setCancelable(false)
        .setIcon(R.mipmap.ic_launcher)
        .setTitle(title)
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
        });
    LayoutInflater factory = LayoutInflater.from(c);
    builder.setView(factory.inflate(R.layout.content_dialog_simple_text, null));
    final AlertDialog dialog = builder.create();
    dialog.show();
    EditText et = dialog.findViewById(R.id.editText);
    TextInputLayout layout = dialog.findViewById(R.id.tilEditText);
    if (et != null && layout != null) {
      et.setText(defaultValue);
      et.addTextChangedListener(new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          // nothing to do
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          layout.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
          // nothing to do
        }
      });
    }
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v) -> positiveClick.onClick(dialog, et, layout));
    return dialog;
  }
}
