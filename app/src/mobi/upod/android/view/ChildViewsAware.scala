package mobi.upod.android.view

import android.app.{Activity, Fragment}
import android.support.annotation.IdRes
import android.view.{View, ViewGroup}
import android.widget._

import scala.reflect.ClassTag

trait ChildViewsAware extends Any {

  def findViewResourceById[A <: View](@IdRes id: Int): A

  def childAs[A <: View](@IdRes id: Int)(implicit classTag: ClassTag[A]): A = findViewResourceById[A](id) match {
    case view: A => view
    case view: View => throw new NoSuchElementException(s"View with ID $id is of type ${view.getClass}, but expected type was ${classTag.runtimeClass}")
    case _ => throw new NoSuchElementException(s"Cannot find view with ID $id")
  }

  def optionalChildAs[A <: View](@IdRes id: Int)(implicit classTag: ClassTag[A]): Option[A] = findViewResourceById[A](id) match {
    case view: A => Some(view)
    case _ => None
  }

  def childView(id: Int) = childAs[View](id)

  def childButton(id: Int) = childAs[Button](id)

  def childToggleButton(id: Int) = childAs[ToggleButton](id)

  def childCheckBox(id: Int) = childAs[CheckBox](id)

  def childTextView(id: Int) = childAs[TextView](id)

  def childImageView(id: Int) = childAs[ImageView](id)

  def childProgressBar(id: Int) = childAs[ProgressBar](id)

  def childSeekBar(id: Int) = childAs[SeekBar](id)

  def childGridView(id: Int) = childAs[GridView](id)

  def childViewGroup(id: Int) = childAs[ViewGroup](id)

  implicit def viewToRichView(view: View) = new Helpers.RichView(view)
}

trait ChildViewsActivity extends ChildViewsAware {
  this: Activity =>

  def findViewResourceById[A <: View](@IdRes id: Int): A = findViewById(id)
}

trait ChildViewsView extends ChildViewsAware {
  this: View =>

  def findViewResourceById[A <: View](@IdRes id: Int): A = findViewById(id)
}

trait ChildViewsFragment extends ChildViewsAware {
  this: Fragment =>

  def findViewResourceById[A <: View](@IdRes id: Int): A = getActivity.findViewById(id)
}
