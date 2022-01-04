/*
 * <Palmcalc is a multipurpose application consisting of calculators, converters
 * and world clock> Copyright (C) <2013> <Cybrosys Technologies pvt. ltd.>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package com.github.palmcalc2019;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.github.palmcalc2019.palmcalc.R;

/**fragment showing the About screen*/
public class AboutAppFragment extends Fragment {

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		return inflater.inflate(R.layout.about_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

        TextView webAbout = (TextView) getView().findViewById(R.id.about_textview);
		webAbout.setText(Html.fromHtml(getString(R.string.about_text)));

        //ImageButton btnlikeus = (ImageButton) getView().findViewById(R.id.goto_issues_button);
		AppCompatButton btnlikeus = getView().findViewById(R.id.goto_issues_button);
		btnlikeus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
				myWebLink.setData(Uri.parse("http://www.github.com/Palmcalc2019/PalmCalc/issues"));
				startActivity(myWebLink);
			}
		});
	}
}
