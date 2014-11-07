package com.example.takephoto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class MainActivity extends Activity {

	private static final int REQUEST_CODE_TAKE_PHOTO = 0;
	private static final int REQUEST_CODE_GALLERY = 1;
	private ImageView imageview1;
	private LinearLayout linearLayout1;
	private Uri extraOutput;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Parse.initialize(this, "pTm5TEfLsTKhtn0mxEly1oIpa5lZgDczNmum8PY3",
				"9W4L6TXeaSHXuMZnHqwYKiAW987ieCPsRPPlidxl");

		imageview1 = (ImageView) findViewById(R.id.imageView1);
		linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
		progressDialog = new ProgressDialog(this);
		
		loadPhotoFromParse();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.action_take_photo) {

			extraOutput = getUri();
			Intent intent = new Intent();
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

			intent.putExtra(MediaStore.EXTRA_OUTPUT, extraOutput);
			startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
			return true;
		} else if (id == R.id.action_gallery) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, REQUEST_CODE_GALLERY);

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ((requestCode == REQUEST_CODE_TAKE_PHOTO)) {
			// Bitmap bitmap = data.getParcelableExtra("data");
			// imageview1.setImageBitmap(bitmap);

//			imageview1.setImageURI(extraOutput);
			saveToParse(extraOutput);
		} else if ((requestCode == REQUEST_CODE_GALLERY)) {
			Uri selectImageUri = data.getData();
			imageview1.setImageURI(selectImageUri);
			saveToParse(selectImageUri);
		}
	}
	

	private Uri getUri() {
		File file = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (file.exists() == false) {
			file.mkdirs();
		}
		File file2 = new File(file, "image.png");
		return Uri.fromFile(file2);
	}

	private byte[] uriToBytes(Uri uri) {
		try {
			InputStream is = getContentResolver().openInputStream(uri);
			ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = is.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, len);
			}
			return byteBuffer.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void saveToParse(Uri uri) {
		byte[] bytes = uriToBytes(uri);
		ParseObject object = new ParseObject("Photo");
		final ParseFile file = new ParseFile("photo.png", bytes);

		object.put("file", file);
		object.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				loadPhotoFromParse();
			}
		});
	}

	private void loadPhotoFromParse() {
		progressDialog.setTitle("Loading...");
		progressDialog.show();
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Photo");
		query.orderByDescending("createAt");
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				
				linearLayout1.removeAllViews();
				
				for (ParseObject object : objects) {
					ParseFile file = object.getParseFile("file");
					Log.d("debug", file.getName());
					
					try {
						byte[] data = file.getData();
						Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						
						ImageView imageView = new ImageView(MainActivity.this);
						imageView.setImageBitmap(bitmap);
						
						linearLayout1.addView(imageView);
						
						Log.d("debug", file.getName());
					} catch (ParseException e1) {
						e1.printStackTrace();
					}

				}
				progressDialog.dismiss();
			}
		});
	}

}
