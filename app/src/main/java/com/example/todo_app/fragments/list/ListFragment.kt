package com.example.todo_app.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.*
import com.example.todo_app.R
import com.example.todo_app.data.models.ToDoData
import com.example.todo_app.data.viewmodel.ToDoViewModel
import com.example.todo_app.databinding.FragmentListBinding
import com.example.todo_app.fragments.SharedViewModel
import com.example.todo_app.fragments.list.adapter.ListAdapter
import com.example.todo_app.utils.hideKeyboard
import com.example.todo_app.utils.observeOnce
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator


class ListFragment : Fragment(), SearchView.OnQueryTextListener {
    private val mToDoViewModel : ToDoViewModel by viewModels()
    private val mSharedViewModel : SharedViewModel by viewModels()

    private var _binding : FragmentListBinding?=null
    private val binding get()=_binding!!

    private val adapter : ListAdapter by lazy { ListAdapter( ) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Data binding
        _binding=FragmentListBinding.inflate(inflater,container,false)
        binding.lifecycleOwner=this
        binding.mSharedViewModel=mSharedViewModel

        //setup recycler view
        setupRecyclerView()

        // Observe LiveData
        mToDoViewModel.getAllData.observe(viewLifecycleOwner) { data ->
            mSharedViewModel.ifDatabaseEmpty(data)
            adapter.setData(data)
        }



        hideKeyboard(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.list_fragment_menu, menu)

                val search = menu.findItem(R.id.menu_search)
                val searchView = search.actionView as? SearchView
                searchView?.isSubmitButtonEnabled = true
                searchView?.setOnQueryTextListener(this@ListFragment)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menu_delete_all -> confirmRemoval()
                    R.id.menu_priority_high ->
                        mToDoViewModel.sortByHighPriority.observe(viewLifecycleOwner) {
                            adapter.setData(it)
                        }
                    R.id.menu_priority_low ->
                        mToDoViewModel.sortByLowPriority.observe(viewLifecycleOwner) {
                            adapter.setData(it)
                        }
                    android.R.id.home -> requireActivity().onBackPressed()
                }
                return true
            }
        },viewLifecycleOwner, Lifecycle.State.RESUMED)

    }

    private fun setupRecyclerView() {
        val recyclerView  = binding.recyclerView
        recyclerView.adapter=adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        recyclerView.itemAnimator = SlideInUpAnimator().apply{
            addDuration=300
        }
        //swipe to delete
        swipeToDelete(recyclerView)


    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback= object : SwipeToDelete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                 val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                //delete item
                 mToDoViewModel.deleteItem(deletedItem)
                 adapter.notifyItemRemoved(viewHolder.adapterPosition)
                 //restore deleted item
                restoreDeletedData(viewHolder.itemView,deletedItem)

            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView )
    }

    private fun restoreDeletedData(view : View, deletedItem : ToDoData) {
        val snackBar = Snackbar.make(view,"Deleted '${deletedItem.title }'",Snackbar.LENGTH_LONG)
        snackBar.setAction("Undo") {
             mToDoViewModel.insertData(deletedItem)

        }
        snackBar.show()

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query!=null) {
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query : String) {
        val searchQuery = "%$query%"

        mToDoViewModel.searchDatabase(searchQuery).observeOnce(viewLifecycleOwner){ list->
            list?.let{
                adapter.setData(it)
            }
        }
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if(query!=null) {
            searchThroughDatabase(query)
        }
        return true
    }

    //show alert dialog to remove all tasks
    private fun confirmRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes"){_,_->
            mToDoViewModel.deleteAll()
            Toast.makeText(requireContext(),"All tasks deleted successfully!",
                Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No"){_,_->}
        builder.setTitle("Delete everything?")
        builder.setMessage("Are you sure you want to delete all tasks?")
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}