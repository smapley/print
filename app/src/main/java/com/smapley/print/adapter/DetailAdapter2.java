package com.smapley.print.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smapley.print.R;

import java.util.List;
import java.util.Map;

/**
 * Created by hao on 2015/11/9.
 */
public class DetailAdapter2 extends BaseAdapter {

    private List<Map<String, String>> list;
    private LayoutInflater inflater;
    private Context context;

    public DetailAdapter2(Context context, List<Map<String, String>> list) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Map<String, String> map = list.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.detail_item2, null);
            viewHolder = new ViewHolder();
            viewHolder.num = (TextView) convertView.findViewById(R.id.detail_item_num);
            viewHolder.gold = (TextView) convertView.findViewById(R.id.detail_item_gold);
            viewHolder.pei = (TextView) convertView.findViewById(R.id.detail_item_pei);
            viewHolder.zt = (TextView) convertView.findViewById(R.id.detail_item_zt);
            viewHolder.allid=(TextView)convertView.findViewById(R.id.detail_item_allid);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.num.setText(map.get("number"));
        viewHolder.gold.setText(map.get("gold"));
        viewHolder.pei.setText(map.get("cgold"));
        viewHolder.zt.setText(map.get("pei"));
        viewHolder.allid.setText("编号："+map.get("allid"));

        return convertView;
    }

    public class ViewHolder {
        TextView num;
        TextView gold;
        TextView pei;
        TextView zt;
        TextView allid;
    }
}
