package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * KML Geometry. This is an abstract class. 
 * @author M.Kergall
 *
 */
public abstract class KmlGeometry implements Cloneable, Parcelable {
	/** possible KML Geometry type */
	public final static int UNKNOWN=0, POINT=1, LINE_STRING=2, POLYGON=3;
	
	/** KML Geometry type */
	public int mType;
	/** id attribute, if any. Null if none. */
	public String mId;
	/** coordinates of the geometry. If Point, one and only one entry. */
	public ArrayList<GeoPoint> mCoordinates;
	
	public KmlGeometry(){
		mType = UNKNOWN;
	}
	
	public boolean isA(int geomType){
		return (mType == geomType);
	}

	/**
	 * Write a list of coordinates in KML format. 
	 * @param writer
	 * @param coordinates
	 * @return false if error
	 */
	public boolean writeKMLCoordinates(Writer writer, ArrayList<GeoPoint> coordinates){
		try {
			writer.write("<coordinates>");
			for (GeoPoint coord:coordinates){
				writer.write(coord.toInvertedDoubleString());
				writer.write(' ');
			}
			writer.write("</coordinates>\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static JSONArray geoJSONPosition(GeoPoint position){
		try {
			JSONArray json = new JSONArray();
			json.put(position.getLongitude());
			json.put(position.getLatitude());
			//json.put(coord.getAltitude()); //don't add altitude, as OpenLayers doesn't supports it... (vertigo?)
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Write a list of coordinates in GeoJSON format. 
	 * @param writer
	 * @param coordinates
	 * @return false if error
	 */
	public static JSONArray geoJSONCoordinates(ArrayList<GeoPoint> coordinates){
		JSONArray json = new JSONArray();
		Iterator<GeoPoint> it = coordinates.iterator();
		while(it.hasNext()) {
			GeoPoint position = it.next();
			json.put(KmlGeometry.geoJSONPosition(position));
		}
		return json;
	}
	
	public static ArrayList<GeoPoint> cloneArrayOfGeoPoint(ArrayList<GeoPoint> coords){
		ArrayList<GeoPoint> result = new ArrayList<GeoPoint>(coords.size());
		for (GeoPoint p:coords)
			result.add((GeoPoint)p.clone());
		return result;
	}
	
	// abstract methods
	public abstract void saveAsKML(Writer writer);
	public abstract JSONObject asGeoJSON();

	public static GeoPoint parseGeoJSONPosition(JSONArray json){
		return new GeoPoint(json.optDouble(1, 0.0), 
				json.optDouble(0, 0.0), 
				json.optDouble(2, 0.0));
	}
	
	public static ArrayList<GeoPoint> parseGeoJSONPositions(JSONArray json){
		if (json == null)
			return null;
		ArrayList<GeoPoint> coordinates = new  ArrayList<GeoPoint>(json.length());
		for (int i=0; i<json.length(); i++){
			JSONArray position = json.optJSONArray(i);
			GeoPoint p = KmlGeometry.parseGeoJSONPosition(position);
			if (p != null)
				coordinates.add(p);
		}
		return coordinates;
	}
	
	public static KmlGeometry parseGeoJSON(JSONObject json){
		if (json == null)
			return null;
		String type = json.optString("type");
		if ("Point".equals(type)){
			return new KmlPoint(json);
		} else if ("LineString".equals(type)){
			return new KmlLineString(json);
		} else if ("Polygon".equals(type)){
			return new KmlPolygon(json);
		} else 
			return null;
	}
	
	//Cloneable implementation ------------------------------------
	
	@Override public KmlGeometry clone(){
		KmlGeometry kmlGeometry = null;
		try {
			kmlGeometry = (KmlGeometry)super.clone();
		} catch (CloneNotSupportedException e){
			e.printStackTrace();
			return null;
		}
		if (mCoordinates != null)
			kmlGeometry.mCoordinates = cloneArrayOfGeoPoint(mCoordinates);
		return kmlGeometry;
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mType);
		out.writeString(mId);
		out.writeList(mCoordinates);
	}
	
	public KmlGeometry(Parcel in){
		mType = in.readInt();
		mId = in.readString();
		mCoordinates = in.readArrayList(GeoPoint.class.getClassLoader());
	}
}