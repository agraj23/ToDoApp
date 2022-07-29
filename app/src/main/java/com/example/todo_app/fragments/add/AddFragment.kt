package com.example.todo_app.fragments.add

import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.todo_app.R
import com.example.todo_app.data.models.ToDoData
import com.example.todo_app.data.viewmodel.ToDoViewModel
import com.example.todo_app.databinding.FragmentAddBinding
import com.example.todo_app.fragments.SharedViewModel

class AddFragment : Fragment() {
     private val mToDoViewModel : ToDoViewModel by viewModels()
     private val mSharedViewModel : SharedViewModel by viewModels()

    private var _binding : FragmentAddBinding?=null
    private val binding get() = _binding!!

     override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         // Data binding
         _binding=FragmentAddBinding.inflate(layoutInflater,container,false)


         binding.prioritiesSpinner.onItemSelectedListener=mSharedViewModel.listener

         return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.menu_add) {
                    insertDataToDb()
                }
                else if(menuItem.itemId==android.R.id.home) {
                    requireActivity().onBackPressed()
                }
                return true
            }
        },viewLifecycleOwner,Lifecycle.State.RESUMED)
    }

    private fun insertDataToDb() {
        val mTitle =_binding!!.titleEt.text.toString()
        val mPriority = binding.prioritiesSpinner.selectedItem.toString()
        val mDescription = binding.descriptionEt.text.toString()
        val validation : Boolean =mSharedViewModel.verifyDataFromUser(mTitle,mDescription)
        if(validation) {
            //Insert data only if title and description fields are not empty
            val newData = ToDoData(
                 0,
                mTitle,
                mSharedViewModel.parsePriority(mPriority),
                mDescription
            )
            mToDoViewModel.insertData(newData)
            Toast.makeText(requireContext(),"Successfully added!",Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        }
        else {
            Toast.makeText(requireContext(),"Please fill out all the fields",Toast.LENGTH_LONG).show()

        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}