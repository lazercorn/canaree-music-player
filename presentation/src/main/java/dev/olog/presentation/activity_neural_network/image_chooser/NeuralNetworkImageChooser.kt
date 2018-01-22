package dev.olog.presentation.activity_neural_network.image_chooser

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import dev.olog.presentation.R
import dev.olog.presentation._base.BaseDialogFragment
import dev.olog.presentation.activity_neural_network.NeuralNetworkActivityViewModel
import dev.olog.presentation.utils.extension.makeDialog
import dev.olog.shared.unsubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class NeuralNetworkImageChooser : BaseDialogFragment() {

    companion object {
        const val TAG = "NeuralNetworkImageChoiser"

        fun newInstance(): NeuralNetworkImageChooser {
            return NeuralNetworkImageChooser()
        }
    }

    private lateinit var adapter : NeuralNetworkImageChooserAdapter
    @Inject lateinit var viewModel: NeuralNetworkActivityViewModel
    private var disposable: Disposable? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(activity!!)
        val view : View = inflater.inflate(R.layout.dialog_list, null, false)

        val builder = AlertDialog.Builder(context)
                .setTitle("choice an image to blend") // todo resources
                .setView(view)
                .setNegativeButton(R.string.popup_negative_cancel, null)
                .setPositiveButton(R.string.popup_positive_save, null)

        val list = view.findViewById<RecyclerView>(R.id.list)

        val dialog = builder.makeDialog()

        adapter = NeuralNetworkImageChooserAdapter(dialog, viewModel)
        list.adapter = adapter
        list.layoutManager = GridLayoutManager(context, 2)

        disposable = viewModel.getImagesAlbum
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter::updateData, Throwable::printStackTrace)

        return dialog
    }

    override fun onStop() {
        super.onStop()
        disposable.unsubscribe()
    }

}