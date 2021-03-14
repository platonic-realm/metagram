package vp.metagram.ui.ReportUI.reportViewer.listSource;

import android.database.MatrixCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import vp.metagram.ui.ReportUI.reportViewer.types.ReportUser;

import static vp.metagram.general.variables.dbMetagram;



public class YouUnfollowedSource extends ReportListSource
{
    long FIPK = -1;
    String searchValue;
    int lastID = -1;


    String QueryFromPart = " from Rel_Following Left Join Others\n" +
                            "    On FollowingIPK = Others.IPK\n" +
                            " Where Rel_Following.StatJobID < %d and Rel_Following.FIPK = %d  and Username like '%%%s%%'";


    String getLastIDQuery = "Select Statistics_Jobs.StatJobID as lastID from Statistics_Orders left Join Statistics_Jobs\n" +
            "      On Statistics_Jobs.StatJobID = \n" +
            "    (Select StatJobID from Statistics_Jobs Where Statistics_Jobs.StatOrderID = Statistics_Orders.StatOrderID and Status = 'done'  order by StatJobID desc limit 1) \n" +
            "      where Statistics_Orders.IPK = %d";



    public YouUnfollowedSource(long FIPK, List<ReportUser> sourceList, String searchValue)
    {
        super(sourceList);
        this.FIPK = FIPK;
        this.searchValue = searchValue;

        clearList();

    }

    public void getLastID() throws Exception
    {
        String sqlText = String.format(Locale.ENGLISH,getLastIDQuery,FIPK);
        MatrixCursor result = dbMetagram.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            lastID = result.getInt(result.getColumnIndex("lastID"));
        }
    }

    public void calculateCount() throws Exception
    {
        String sqlText = String.format(Locale.ENGLISH,"Select Count(*) as No " + QueryFromPart,lastID, FIPK,searchValue);

        MatrixCursor result = dbMetagram.selectQuery(sqlText);
        if (result.moveToFirst())
        {
            count = result.getInt(result.getColumnIndex("No"));
        }

    }

    @Override
    public List<ReportUser> getNextList() throws Exception
    {

        getLastID();


        if (FIPK < 0)
        {
            clearList();
        }
        else
        {

            List<ReportUser> newList = new ArrayList<>();

            String sqlText = String.format(Locale.ENGLISH,"Select IPK, Username, PictureURL " + QueryFromPart + limitQuery,
                    lastID, FIPK,searchValue,pageCapacity,index);

            MatrixCursor result = dbMetagram.selectQuery(sqlText);
            if (result.moveToFirst())
            {
                while ( !result.isAfterLast() )
                {
                    ReportUser reportUser = new ReportUser();
                    reportUser.IPK = result.getLong(result.getColumnIndex("IPK"));
                    reportUser.username = result.getString(result.getColumnIndex("Username"));
                    reportUser.picUrl = result.getString(result.getColumnIndex("PictureURL"));
                    newList.add(reportUser);
                    result.moveToNext();
                }
            }

            sourceList.addAll(newList);
            index += pageCapacity;
        }

        return sourceList;
    }


    @Override
    public void setSearchValue(String searchValue)
    {
        this.searchValue = searchValue;
    }
}