package ru.kapu.bluetoothremotecontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Created by kapus on 11.12.2015.
 */
public class CodesDatabase extends SQLiteOpenHelper {

    //! Database Version
    private static final int DATABASE_VERSION = 1;
    //! Database Name
    private static final String DATABASE_NAME = "CodesDB";

    public CodesDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE codes " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "description TEXT, " +
                "code_name TEXT, " +
                "type_name TEXT, " +
                "code TEXT);");

        db.execSQL("INSERT INTO codes VALUES ( null, \"Code name 1\", \"CAME12\", null, \"000000000000019A\");");
        db.execSQL("INSERT INTO codes VALUES ( null, \"Code name 2\", \"KEELOQ\", \"Doorkhan\", \"47253E114864EA65\");");
        db.execSQL("INSERT INTO codes VALUES ( null, \"Code name 3\", \"GSN\", null, \"0000000011223344\");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS codes");

        // create fresh books table
        this.onCreate(db);
    }

    public Cursor GetAllCodes()
    {
        return getReadableDatabase().rawQuery("SELECT  * FROM codes", null);
    }

    public void AddNewCode(String desc, String name, String type, String code)
    {
        SQLiteDatabase db = getWritableDatabase();
        String sql_query ="INSERT INTO codes VALUES ( null, ?, ?, ?, ?);";
        if (type!=null && type.isEmpty())
        {
            type=null;
        }
        db.execSQL(sql_query, new String[] {desc, name, type, code} );
    }

    public void EditCode(Integer id, String desc, String name, String type, String code)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("description", desc);
        cv.put("code_name", name);
        if (type!=null && !type.isEmpty())
        {
            cv.put("type_name", type);
        }
        cv.put("code", code);

        db.update("codes", cv, "_id=?", new String[]{String.valueOf(id)});
    }

    public void UpdateCodeOnly(Integer id, String code)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("code", code);

        db.update("codes", cv, "_id=?", new String[]{String.valueOf(id)});
    }

    public void RemoveCodeById(int id)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("codes", "_id=?", new String[]{String.valueOf(id)});
    }

    public Cursor GetCursorById(Integer id)
    {
        return getReadableDatabase().rawQuery("SELECT  * FROM codes WHERE (_id = ?)", new String[] {String.valueOf(id)});
    }

    public String GetFieldById(Integer id, String field)
    {
        String res = null;

        Cursor c = GetCursorById(id);
        if (c != null && c.moveToFirst())
        {
            res = c.getString(c.getColumnIndexOrThrow(field));
        }
        return res;
    }

    public String ExportToCSV()
    {
        File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
        if (!exportDir.exists())
        {
            if (!exportDir.mkdirs())
            {
                Log.e("BRC", "Directory not created");
                return "Directory not created";
            }
        }
        File file = new File(exportDir, "codes.csv");
        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curCSV = GetAllCodes();
            csvWrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext())
            {
                //Which column you want to exprort
                String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2), curCSV.getString(3), curCSV.getString(4)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
        }
        catch(Exception ex)
        {
            Log.e("BRC", ex.getMessage(), ex);
            return ex.getMessage();
        }

        return "Exported to " + file.getAbsolutePath();
    }

    public String ImportFromCSV()
    {
        File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
        if (!exportDir.exists())
        {
            return "Directory " + exportDir.getAbsolutePath() + " did not exist";
        }

        File file = new File(exportDir, "codes.csv");
        try
        {
            FileReader fileReader = new FileReader(file);
            CSVReader csvReader = new CSVReader(fileReader);
            String [] nextLine;

            while ((nextLine = csvReader.readNext()) != null) {
                try {
                    Integer.parseInt(nextLine[0]);
                    AddNewCode(nextLine[1], nextLine[2], nextLine[3], nextLine[4]);
                } catch(NumberFormatException nfe) {
                }             }
        }
        catch(Exception ex)
        {
            Log.e("BRC", ex.getMessage(), ex);
            return ex.getMessage();
        }

        return "Imported from " + file.getAbsolutePath();
    }
}
