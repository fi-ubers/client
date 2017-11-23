package com.example.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A list-shaped screen showing the driver's cars. Allows driver to update, delete and add cars.
 */
public class CarsActivity extends Activity {
	CarsAdapter adapter;
	EditText editModel, editNumber;
	ArrayList<CarInfo> arrayList;
	ListView listView;

	/**
	 * Activity onCreate method.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cars);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		listView = (ListView) findViewById(R.id.listv);

		arrayList = UserInfo.getInstance().getCars();
		adapter = new CarsAdapter(this, R.layout.cars_item, arrayList);
		listView.setAdapter(adapter);


		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Show input box
				showInputBox(arrayList.get(position).getModel(), arrayList.get(position).getNumber(), position);
			}
		});

		editModel = (EditText) findViewById(R.id.inputModel);
		editNumber = (EditText) findViewById(R.id.inputNumber);
		Button btAdd = (Button) findViewById(R.id.btAdd);
		btAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String newModel = editModel.getText().toString();
				String newNumber = editNumber.getText().toString();
				// Add new item to arraylist
				// Must first POST to app-server
				UserInfo ui = UserInfo.getInstance();
				try {
					CarsHandler carHandler = new CarsHandler();
					ConexionRest conn = new ConexionRest(carHandler);
					String carsUrl = conn.getBaseUrl() + "/users/" + Integer.toString(ui.getIntegerId());
					carsUrl = carsUrl + "/cars";
					Log.d("CarsActivity", "URL to PUT: " + carsUrl);
					// Next step ugly
					String toSendJson = "{ \"name\": \"" + newModel + "\", \"value\": \"" + newNumber + "\" }";
					Log.d("CarsActivity", "JSON to send (POST car): " + toSendJson);
					conn.generatePost(toSendJson, carsUrl, null);
				}
				catch(Exception e){
					Log.e("CarsActivity", "Sunmitting PUT error: ", e);
				}
				editModel.setText("");
				editNumber.setText("");
			}
		});

	}

	/**
	 * Overrided method for returning to parent {@link Activity}.
	 * @param item {@link MenuItem} clicked on {@link android.app.ActionBar}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Log.d("ProfileActivity", "Back button pressed on actionBar");
		ActivityChanger.getInstance().gotoActivity(CarsActivity.this, ProfileActivity.class);
		finish();
		return true;

	}

	/**
	 * Shows a dialog input box with a selected car data at listView index position.
	 * It is used for updating the car's model and number.
	 * @param oldModel Model of the car before data updating
	 * @param oldNumber Number of the car before data updating
	 * @param index Position of the selected car in arrayList
	 */
	public void showInputBox(String oldModel, String oldNumber, final int index){
		final Dialog dialog = new Dialog(CarsActivity.this);
		dialog.setTitle("Update your car data");
		dialog.setContentView(R.layout.cars_input_box);
		TextView txtMessage=(TextView)dialog.findViewById(R.id.txtmessage);
		txtMessage.setText("Change your data");
		txtMessage.setTextColor(Color.parseColor("#ff2222"));
		final EditText editModel = (EditText)dialog.findViewById(R.id.carModel);
		final EditText editNumber = (EditText)dialog.findViewById(R.id.carNumber);
		editModel.setText(oldModel);
		editNumber.setText(oldNumber);
		// Edit car
		Button bt=(Button)dialog.findViewById(R.id.btdone);
		bt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String carModel = editModel.getText().toString();
				String carNumber = editNumber.getText().toString();
				int carId = arrayList.get(index).getId();
				// PUT changes to app-server
				CarInfo carModified = new CarInfo(carModel, carNumber, carId);
				arrayList.set(index, carModified);
				UserInfo ui = UserInfo.getInstance();
				try {
					ConexionRest conn = new ConexionRest(null);
					String carsUrl = conn.getBaseUrl() + "/users/" + Integer.toString(ui.getIntegerId());
					carsUrl = carsUrl + "/cars/";
					carsUrl = carsUrl + Integer.toString(carId);
					Log.d("CarsActivity", "URL to PUT: " + carsUrl);
					Jsonator jnator = new Jsonator();
					String toSendJson = jnator.writeCarInfo(carModified);
					Log.d("CarsActivity", "JSON to send (PUT car): " + toSendJson);
					conn.generatePut(toSendJson, carsUrl, null);
				}
				catch(Exception e){
					Log.e("CarsActivity", "Submitting PUT error: ", e);
				}

				adapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		});

		Button btDel=(Button)dialog.findViewById(R.id.btdelCar);
		btDel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String carModel = editModel.getText().toString();
				String carNumber = editNumber.getText().toString();
				int carId = arrayList.get(index).getId();
				// Remove car from list and DELETE via app-server
				UserInfo ui = UserInfo.getInstance();
				try {
					ConexionRest conn = new ConexionRest(null);
					String carsUrl = conn.getBaseUrl() + "/users/" + Integer.toString(ui.getIntegerId());
					carsUrl = carsUrl + "/cars/";
					carsUrl = carsUrl + Integer.toString(carId);
					Log.d("CarsActivity", "URL to DELETE car: " + carsUrl);
					conn.generateDelete(carsUrl, null);
				}
				catch(Exception e){
					Log.e("CarsActivity", "Submitting DELETE error: ", e);
				}
				arrayList.remove(index);
				adapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		});

		dialog.show();
	}

// -------------------------------------------------------------------------------------
/**
 * A {@link ListView} adapter based on {@link ArrayAdapter}, specifically made for
 * the various driver's {@link CarInfo}.
 */
public class CarsAdapter extends ArrayAdapter<CarInfo>{

	Context context;
	int layoutResourceId;
	ArrayList<CarInfo> data = null;

	/**
	 * Class constructor.
	 * @param context Current {@link Context} for this adapter
	 * @param layoutResourceId Integer id of an {@link ImageView} to use as bullet in the list
	 * @param data {@link CarInfo} list of driver's cars
	 */
	public CarsAdapter(Context context, int layoutResourceId, ArrayList<CarInfo> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	/**
	 * Overrided getView method (inflates and populates {@link ListView}).
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		CarHolder holder = null;

		if(row == null)
		{
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new CarHolder();
			holder.imgIcon = (ImageView) row.findViewById(R.id.carIcon);
			holder.txtTitle = (TextView)row.findViewById(R.id.txtitem);


			row.setTag(holder);
		}
		else
		{
			holder = (CarHolder)row.getTag();
		}

		CarInfo car = data.get(position);
		holder.txtTitle.setText(car.getModel() + " - " + car.getNumber());

		return row;
	}

	/**
	 * Auxiliar class to hold {@link CarInfo} data.
	 */
	public class CarHolder {
			ImageView imgIcon;
			TextView txtTitle;
		}
	}

// ---------------------------------------------------------------------------------------

	/**
	 * A class for handling app-server responses for cars POST.
	 */
	public class CarsHandler implements RestUpdate{

		/**
		 * Class default constructor.
		 */
		public CarsHandler(){

		}

		/**
		 * Checks if the driver could POST their new car, and adds the respective
		 * {@link CarInfo} to arrayList.
		 * @param servResponse The app-server's response to the sign up request
		 */
		@Override
		public void executeUpdate(String servResponse) {
			Jsonator jnator = new Jsonator();
			CarInfo newCar = jnator.readCarInfo(servResponse, true);
			if(newCar == null) return;
			// notify listview of data changed
			arrayList.add(newCar);
			adapter.notifyDataSetChanged();
			return;

		}
	}
}