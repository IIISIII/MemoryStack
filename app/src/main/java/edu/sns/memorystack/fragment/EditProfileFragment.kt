package edu.sns.memorystack.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import edu.sns.memorystack.R


class EditProfileFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.profile_edit, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        val editNickname = view.findViewById<EditText>(R.id.editNickname)
        val saveProfile = view.findViewById<Button>(R.id.save_profile)
    }


}