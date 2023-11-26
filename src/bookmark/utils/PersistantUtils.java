package bookmark.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.google.gson.Gson;

import bookmark.views.TreeParent;

public class PersistantUtils {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "bookmark.views.BookmarkView";
	public static final String DATA_STORE_KEY = "bookmark_datasource";

	/**
	 * Use eclipse Preferences API to make data persistent
	 *
	 * @param dataSource
	 */
	public static void savePersistantData(TreeParent dataSource) {
		Preferences prefs = InstanceScope.INSTANCE.getNode(ID);

		// change object to string
		Gson gson = new Gson();

		// change object byte array
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o;
		try {
			o = new ObjectOutputStream(b);
			o.writeObject(dataSource);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		byte[] byteDataArray = b.toByteArray();

		// use gson to change byte array to string
		String json_str = gson.toJson(byteDataArray);

		prefs.put(DATA_STORE_KEY, json_str);
		try {
			// store to disk
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	public static TreeParent loadPersistantData() {
		Preferences prefs = InstanceScope.INSTANCE.getNode(ID);

		String json_str = prefs.get(DATA_STORE_KEY, "");

		if (json_str == "") {
			// no data source yet, do initialization
			TreeParent invisibleRoot = new TreeParent("");
			return invisibleRoot;
		} else {
			Gson gson = new Gson();
			byte[] byteDataArray = gson.fromJson(json_str, byte[].class);

			// deserialize object from byteDataArray
			ByteArrayInputStream b = new ByteArrayInputStream(byteDataArray);
			ObjectInputStream o;
			TreeParent invisibleRoot = null;
			try {
				o = new ObjectInputStream(b);
				invisibleRoot = (TreeParent) o.readObject();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return invisibleRoot;
		}
	}

}
