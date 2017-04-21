package me.bemind.glitchappcore.history

import android.graphics.Bitmap
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.bemind.glitchappcore.ImageDescriptor
import java.util.ArrayList
import java.util.concurrent.Callable

/**
 * Created by angelomoroni on 14/04/17.
 */

interface IHistoryPresenter {

    var hasHistory :Boolean

    var canBack : Boolean

    fun clearHistory()

    fun addImage(bitmap: Bitmap,newPhoto:Boolean = false)

    fun back()

    fun getHistoryToSave(): ArrayList<ImageDescriptor>?

    fun restoreHistory(list: ArrayList<ImageDescriptor>?,setImage:Boolean = true)


}

class HistoryPresenter : IHistoryPresenter {

    val historyLogic  = HistoryLogic()
    var historyView : IHistoryView? = null

    private var disposable: Disposable? = null

    override var hasHistory: Boolean
        get() = historyLogic.hasHistory
        set(value) {/*nothing*/}
    override var canBack: Boolean
        get() = historyLogic.canBack()
        set(value) {}

    override fun clearHistory() {
       historyLogic.clearHistory()
    }

    override fun addImage(bitmap: Bitmap,newPhoto: Boolean) {
        historyLogic.addBitmap(bitmap,newPhoto)
    }

    override fun back() {

        observableImage(
                {
                    historyLogic.back()
                },
                {
                    b -> historyView?.setPreviousImage(b)
                }
        )


    }

    override fun getHistoryToSave(): ArrayList<ImageDescriptor>? {
        disposable?.dispose()
        return historyLogic.getStack()
    }

    override fun restoreHistory(list: ArrayList<ImageDescriptor>?,setImage: Boolean) {
        historyLogic.setStack(list)
        if(setImage){
            observableImage(
                    {
                        historyLogic.lastBitmap
                    },
                    {
                        b -> historyView?.setPreviousImage(b,true)
                    }
            )
        }
    }



    fun observableImage(callableFunction: () -> Bitmap?, nextAction: (t: Bitmap?) -> Unit) {
       disposable =  Observable.fromCallable(callableFunction)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        nextAction,
                        {
                            t -> t.printStackTrace()
                        }
                )
    }
}
