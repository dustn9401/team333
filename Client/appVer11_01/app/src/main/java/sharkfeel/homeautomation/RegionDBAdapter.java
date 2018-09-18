package sharkfeel.homeautomation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
SQLiteOpenHelper클래스 상속받은 DBHelper클래스 정의
DB 파일 이름은 "mycontacts.db"가 되고 DB 버전은 1이다.
만약 DB요청후 DB가 없을시 onCreate()호출해서 DB파일을 생성한다.
 */
public class RegionDBAdapter {

    public static final String KEY_ROWID = "_id";

    public static final String KEY_STEP1 = "step1";
    public static final String KEY_STEP2 = "step2";
    public static final String KEY_STEP3 = "step3";
    public static final String KEY_COORDINATE_X = "coordinate_x";
    public static final String KEY_COORDINATE_Y = "coordinate_y";

    private static final String DATABASE_TABLE = "notes";   //  테이블 이름
    private static final String DATABASE_NAME = "data.db"; //  데이터베이스 이름
    private static final int DATABASE_VERSION = 1;          //  데이터베이스 버전

    private static final String TAG = "RegionDBAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (_id integer primary key autoincrement, "
            + KEY_STEP1 + " text not null, " + KEY_STEP2 + " text not null, " + KEY_STEP3 +" text not null, "
            + KEY_COORDINATE_X + " text not null, " + KEY_COORDINATE_Y + " text not null);";

    private static final String DATABASE_DROP =
            "DROP TABLE IF EXISTS " + DATABASE_TABLE;

    private final Context mCtx;

    /*
        SQLiteOpenHelper클래스 상속받은 DatabaseHelper 클래스 정의
        DB 버전은 1임. DB요청후 DB가 없을시 onCreate()호출해서 DB파일을 생성한다.
    */

    //  헬퍼 클래스 (SQLiteOpenHelper) 데이터베이스 열기/닫기를 담당
    //  DB 생성, 업데이트, 추가, 삭제 구현
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        /*
           onUpgrade()는 DB의 버전이 증가되었을 때 호출됨, 여러 필요한 업그레이드 동작을 수행하는
           것이 바람직하지만, 일단은 무조건 기존 테이블을 버리고 단순히 새로운 테이블 정의하도록 했음
        */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }
    /*
     * Constructor - takes the context to allow the database to be
     * opened/created
     * @param ctx the Context within which to work
     */
    public RegionDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }


    /*
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    //  DB사용하기전에 open해줌(new 해줌)
    public RegionDBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    //  DB 닫기
    public void close() {
        mDbHelper.close();
    }


    /*
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * @param product the title of the note
     * @param waybill the body of the note
     * @return rowId or -1 if failed
     */
    //  새로운 노드(행)를 추가함 (레코드 추가)
    public long createNote(String step1, String step2, String step3, String coordinate_x, String coordinate_y) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_STEP1, step1);
        initialValues.put(KEY_STEP2, step2);
        initialValues.put(KEY_STEP3, step3);
        initialValues.put(KEY_COORDINATE_X, coordinate_x);
        initialValues.put(KEY_COORDINATE_Y, coordinate_y);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }


    /*
     * Delete the note with the given rowId
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    //  인자로 넣는 행의 데이터를 삭제함 (레코드 삭제)
    public boolean deleteNote(long rowId) {
        Log.i("Delete called", "value__" + rowId);
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    //  가장 뒤에 있는 레코드(행)을 삭제함
    public void deleteNote()
    {
        String sql = "DELETE FROM " + DATABASE_TABLE + " WHERE _id = "
                + "(select MAX(_id) from " + DATABASE_TABLE + ")";
        mDb.execSQL(sql);
    }

    /*
    아직 미구현.. string으로 검색해서 지울 때 사용
    public boolean deleteNote(String date) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" +
                "(SELECT _id from " + DATABASE_TABLE + " where " + KEY_DATE + "='"
                + date + "')", null) > 0;
    }

    /*public void deleteNote(String date)
    {
     String sql = "DELETE FROM notes WHERE _id = "
      + "(SELECT _id from notes where date='" + date +"')";
     mDb.execSQL(sql);
    }
    */

    //  fetchAllNotes() 설명
    /*
     * Return a Cursor over the list of all notes in the database
     * @return Cursor over all notes
     * 데이터베이스의 모든 노트 목록에서 커서를 반환해줌
     */
    //  전체 테이블의 행수를 return함 (모든 레코드 반환)
    public Cursor fetchAllNotes() {
        return mDb.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_STEP1, KEY_STEP2,
                KEY_STEP3, KEY_COORDINATE_X, KEY_COORDINATE_Y}, null, null, null, null, null);
    }

    /*
     * Return a Cursor positioned at the note that matches the given rowId
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     * 지정된 rowId와 일치하는 노트에 있는 커서를 반환합니다.
     * 불러 올 노트의 paramrowId ID
     * 찾은 경우 일치하는 노트에 커서를 반환합니다.
     * 노트를 found/retried 할 수 없는 경우 SQLException실행
     */
    //  인자로 넣는 행의 데이터를 가져옴 (Cursor 가 행의 위치)
    //  특정 레코드 반환
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_STEP1, KEY_STEP2,
                KEY_STEP3, KEY_COORDINATE_X, KEY_COORDINATE_Y }, KEY_ROWID
                + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    /*
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    //  테이블 내용 변경
    public boolean updateNote(long rowId, String step1, String step2, String step3, String coordinate_x, String coordinate_y) {
        ContentValues args = new ContentValues();
        args.put(KEY_STEP1, step1);
        args.put(KEY_STEP2, step2);
        args.put(KEY_STEP3, step3);
        args.put(KEY_COORDINATE_X, coordinate_x);
        args.put(KEY_COORDINATE_Y, coordinate_y);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
        /*
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" +
                "(SELECT _id from " + DATABASE_TABLE + " where " + KEY_DATE + "='"
                + date + "')", null) > 0;
        */
    }

    public void dropTable()
    {
        mDb.execSQL(DATABASE_DROP);
    }

    public void createTable()
    {
        mDb.execSQL(DATABASE_CREATE);
    }


}
