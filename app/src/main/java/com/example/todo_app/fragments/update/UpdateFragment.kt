package com.example.todo_app.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todo_app.R
import com.example.todo_app.data.models.ToDoData
import com.example.todo_app.data.viewmodel.ToDoViewModel
import com.example.todo_app.databinding.FragmentUpdateBinding
import com.example.todo_app.fragments.SharedViewModel

class UpdateFragment : Fragment() {
    private val args by navArgs<UpdateFragmentArgs>()
    private val mToDoViewModel : ToDoViewModel by viewModels()
    private val mSharedViewModel : SharedViewModel by viewModels()

    private var _binding : FragmentUpdateBinding ? = null
    private val binding get()= _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding=FragmentUpdateBinding.inflate(inflater,container,false)
        binding.args=args

        val view= inflater.inflate(R.layout.fragment_update, container, false)


        //Spinner item selected listener
        binding.currentPrioritiesSpinner.onItemSelectedListener=mSharedViewModel.listener
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.update_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menu_save -> updateItem()
                    R.id.menu_delete -> confirmItemRemoval()
                    android.R.id.home -> requireActivity().onBackPressed()
                }
                return true
            }
        },viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    private fun updateItem() {
        val mTitle =binding.currentTitleEt.text.toString()
        val mPriority  = binding.currentPrioritiesSpinner.selectedItem.toString()
        val mDescription = binding.currentDescriptionEt.text.toString()
        val validation : Boolean =mSharedViewModel.verifyDataFromUser(mTitle,mDescription)
        if(validation) {
            val updatedItem = ToDoData(
                args.currentItem.id,
                mTitle,
                mSharedViewModel.parsePriority(mPriority),
                mDescription
            )
            mToDoViewModel.updateData(updatedItem)
            Toast.makeText(requireContext(),"Successfully updated!",Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)

        }
        else {
            Toast.makeText(requireContext(),"Please fill out all the fields",
                Toast.LENGTH_LONG).show()

        }
    }

    // show alert dialog to con firm removal
    private fun confirmItemRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes"){_,_->
            mToDoViewModel.deleteItem(args.currentItem)
            Toast.makeText(requireContext(),"Successfully deleted: ${args.currentItem.title}",
            Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No"){_,_->}
        builder.setTitle("Delete '${args.currentItem.title}'?")
        builder.setMessage("Are you sure you want to delete '${args.currentItem.title}'?")
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}