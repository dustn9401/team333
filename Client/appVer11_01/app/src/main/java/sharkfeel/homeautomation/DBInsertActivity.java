package sharkfeel.homeautomation;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class DBInsertActivity extends AppCompatActivity {

    //데이터 베이스 어답터
    static public DBAdapter mDbHelper;

    //리스트뷰 생성, 어댑터
    private ListView listView;
    private Button btnInsert;

    private Long mRowId;
    private static final int DELETE_ID = 0;

    //데이터 베이스에서 유저가 선택한 노트의 String을 리턴해준다 ㅋㅋ
    private String getDBString(AdapterView.AdapterContextMenuInfo info, String selectKey) {
        //모든 음성 검색해서
        Cursor notesCursor = mDbHelper.fetchAllNotes(mDbHelper.KEY_ROWID, "DESC");
        startManagingCursor(notesCursor);
        //포지션에 맞는거 찾으면
        notesCursor.moveToPosition(info.position);

        String labelColumn_body = notesCursor.getString(notesCursor.
                getColumnIndex(selectKey));
        //그거 리턴
        return labelColumn_body;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //설정시 무엇눌렀는지 팝업메뉴 위에 TITLE띄어줌
        AdapterView.AdapterContextMenuInfo info
                = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(getDBString(info, DBAdapter.KEY_SAY));
        menu.add(0, DELETE_ID, 0, "삭제하기");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info
                = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case DELETE_ID:
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbinsert);

//        //DB오픈
        mDbHelper = new DBAdapter (this);
        mDbHelper.open();

        listView = (ListView)findViewById(R.id.sayList);
        fillData();

        //컨텍스트 메뉴 사용
        registerForContextMenu(listView);

        btnInsert = (Button)findViewById(R.id.btnDBInsert);
    }

    //데이터 채우기 (모든 보여주기)
    private void fillData() {
        Cursor notesCursor =  mDbHelper.fetchAllNotes(mDbHelper.KEY_ROWID, "DESC");
        // Get all of the notes from the database and create the item list
        startManagingCursor(notesCursor);

        String[] from = new String[] { DBAdapter.KEY_SAY , DBAdapter.KEY_SERVER, DBAdapter.KEY_URL,
                DBAdapter.KEY_MEANS, DBAdapter.KEY_MESSAGE};

        // Now create an array adapter and set it to display using our row

    }
}