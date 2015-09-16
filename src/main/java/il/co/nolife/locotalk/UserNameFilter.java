package il.co.nolife.locotalk;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import il.co.nolife.locotalk.DataTypes.LocoUser;

/**
 * Created by Victor Belski on 9/16/2015.
 */
public class UserNameFilter extends Filter {

    List<LocoUser> allUsers;

    public UserNameFilter(List<LocoUser> allUsers) {
        this.allUsers = allUsers;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults result = new FilterResults();
        if((constraint != null) && (constraint.length() > 0)){

            String constraintStr = constraint.toString().toLowerCase();
            List<LocoUser> resultingList = new ArrayList<>();

            for (LocoUser user : allUsers) {
                if(user.getName().toLowerCase().contains(constraintStr)) {
                    resultingList.add(user);
                }
            }

            result.count = resultingList.size();
            result.values = resultingList;

        } else {

            result.count = allUsers.size();
            result.values = allUsers;

        }

        return result;

    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {



    }

}
