package per.dizzam.sustc.jwxt.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import per.dizzam.sustc.jwxt.CourseData;
import per.dizzam.sustc.jwxt.CourseRepo;

public class CourseManager {
	
	private static final String[] WEEK = new String[] { "", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
	
	class Course {
		
		private ArrayList<Label> labels;
		private JsonObject course;
		private float hue = 0;
		private boolean isSelected;
		private boolean isChecked;
		private TreeItem item;
		private CourseRepo category;
		
		public Course(JsonObject course, CourseRepo category) {
			labels = new ArrayList<>();
			this.course = course;
			this.category = category;
		}
		
		public void layoutLable() {
			if (!labels.isEmpty()) {
				return;
			}
			hue = picker.getCurrentHue();
			JsonArray times = course.get("kkapList").getAsJsonArray();
			Color color = picker.changeLighten(hue, isSelected);
			for (JsonElement time : times) {
				JsonObject t = time.getAsJsonObject();
				int week = t.get("xq").getAsInt();
				int from = Integer.valueOf(t.get("skjcmc").getAsString().split("-")[0]);
				int to = Integer.valueOf(t.get("skjcmc").getAsString().split("-")[1]);
				Composite composite = weekList.get(week);
				FormData fd_l = new FormData();
				Label label = new Label(composite, SWT.WRAP);
				label.setText(course.get("kcmc").getAsString());
				label.setData(this);
				label.setBackground(color);
				fd_l.top = new FormAttachment((from - 1) * 10, 2);
				fd_l.bottom = new FormAttachment(to * 10, -1);
				int left = 0;
				for (Control control : composite.getChildren()) {
					if (control.getData() != null && control.getData() instanceof Course && !control.equals(label)) {
						FormData data = (FormData) control.getLayoutData();
						if (data.top.numerator < fd_l.bottom.numerator && data.bottom.numerator > fd_l.top.numerator
								&& data.right.offset > left) {
							left = data.right.offset;
						}
					}
				}
				fd_l.left = new FormAttachment(0, left);
				fd_l.right = new FormAttachment(0, left + 15);
				label.setLayoutData(fd_l);
//				FontData fd = new FontData("MyFont", 10, SWT.ITALIC);
//				label.setFont(new Font(scroll.getDisplay(), fd));
				label.moveAbove(null);
				computeSize(composite);
				label.requestLayout();
				label.addMouseTrackListener(new MouseTrackAdapter() {

					@Override
					public void mouseEnter(MouseEvent e) {
						if (!isSelected) {
							lightenLable(true);
						}
					}

					@Override
					public void mouseExit(MouseEvent e) {
						if (!isSelected) {
							lightenLable(false);
						}
					}
				});
				label.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseDown(MouseEvent e) {
						if (isSelected) {
							isSelected = false;
							selected.remove(Course.this);
						} else {
							isSelected = true;
							selected.add(Course.this);
						}
					}
				});
				labels.add(label);
			}
		}
		
		public void disposeLable() {
			for (Label label : labels) {
				label.dispose();
			}
			labels.removeAll(labels);
		}
		
		public void lightenLable(boolean light) {
			for (Label l : labels) {
				l.setBackground(picker.changeLighten(hue, light));							
			}
		}
		
		public void displayItem() {
			TreeItem root = null;
			for (TreeItem item : tree.getItems()) {
				if (item.getData().equals(category)) {
					root = item;
				}
			}
			TreeItem parent = null;
			JsonElement element = course;
			for (TreeItem item : root.getItems()) {
				if (item.getText().equals(element.getAsJsonObject().get("kcmc").getAsString())) {
					parent = item;
				}
			}
			if (parent == null) {
				TreeItem newItem = new TreeItem(root, SWT.NONE);
				newItem.setText(0, element.getAsJsonObject().get("kcmc").getAsString());
				newItem.setText(1, String.valueOf(element.getAsJsonObject().get("xf").getAsInt()));
				JsonElement e = element.getAsJsonObject().get("pgtj");
				newItem.setText(3, e.isJsonNull() ? "无" : e.getAsString());
				parent = newItem;
			}
			item = new TreeItem(parent, SWT.NONE);
			JsonElement e1 = element.getAsJsonObject().get("fzmc");
			item.setText(0, element.getAsJsonObject().get("kcmc").getAsString()
					+ (e1.isJsonNull() ? "" : "[" + e1.getAsString() + "]"));
			item.setText(1, String.valueOf(element.getAsJsonObject().get("xf").getAsInt()));
			JsonElement e2 = element.getAsJsonObject().get("skls");
			item.setText(2, e2.isJsonNull() ? "无" : e2.getAsString());
			JsonElement e3 = element.getAsJsonObject().get("pgtj");
			item.setText(3, e3.isJsonNull() ? "无" : e3.getAsString());
			item.setChecked(isChecked);
			item.setGrayed(false);
			TreeItem head = item;
			while ((parent = head.getParentItem()) != null) {
				if (isChecked) {
					parent.setChecked(true);
					parent.setGrayed(false);
					for (TreeItem item1 : parent.getItems()) {
						if (!item1.getChecked() || item1.getGrayed()) {
							parent.setGrayed(true);
							break;
						}
					}
				} else {
					parent.setGrayed(true);
					parent.setChecked(false);
					for (TreeItem item1 : parent.getItems()) {
						if (item1.getChecked()) {
							parent.setChecked(true);
							break;
						}
					}
				}
				head = parent;
			}
			item.setData(this);
		}
		
		public void disposeItem() {
			if (item != null) {
				TreeItem parent = item.getParentItem();
				item.dispose();
				item = null;
				if (parent.getItems().length == 0) {
					parent.dispose();
				}
			}
		}
	}
	
	private CourseData courseData;
	private Tree tree;
	private ScrolledComposite scroll;
	private ArrayList<Composite> weekList = new ArrayList<>();
	private ArrayList<Course> courses = new ArrayList<>();
	private ArrayList<Course> selected = new ArrayList<>();
	private ColorPicker picker;

	public CourseManager(ScrolledComposite scroll, Tree tree, CourseData courseData) {
		this.tree = tree;
		this.scroll = scroll;
		this.courseData = courseData;
		picker = new ColorPicker(scroll.getDisplay());
		init();
	}
	
	private void computeSize(Composite target) {
		scroll.setMinWidth(target.computeSize(SWT.DEFAULT, SWT.DEFAULT).x * 7 + 20);
//		int max = 0;
//		for (Control control : target.getChildren()) {
//			int tmp = ((FormData)control.getLayoutData()).right.offset;
//			max = max < tmp ? tmp : max;
//		}
//		if (target.getBounds().width < max) {
//			scroll.setMinWidth(max * 7 + 20);
//		}
	}
	
	public ArrayList<Course> searchCourse(String name) {
		if (name == null) {
			return courses;
		}
		ArrayList<Course> target = new ArrayList<>();
		for (Course course : courses) {
			JsonObject jsonObject = course.course;
			if (jsonObject.get("kcmc").getAsString().contains(name)
					|| jsonObject.get("kch").getAsString().contains(name) 
					|| jsonObject.get("jx0404id").getAsString().equals(name)
					|| (jsonObject.get("skls") == null ? false : jsonObject.get("skls").toString().contains(name))) {
				target.add(course);
			}
		}
		return target;
	}
	
	public void updateData(String string) {
		for (Course course : courses) {
			course.disposeItem();
		}
		
		for (Course course : searchCourse(string)) {
			course.displayItem();
		}
		
		for (TreeItem item : tree.getItems()) {
			item.setExpanded(true);
		}
	}
	
	private void init() {
		for (Entry<String, JsonElement> entry : courseData.getCourse().entrySet()) {
			for (JsonElement course : entry.getValue().getAsJsonArray()) {
				Course newCourse = new Course((JsonObject) course, CourseRepo.valueOf(entry.getKey()));
				courses.add(newCourse);
			}
		}
		
		for (JsonElement selected : courseData.getSelected()) {
			for (Course target : searchCourse(selected.getAsString())) {
				target.isSelected = true;
				target.isChecked = true;
				this.selected.add(target);
			}
		}

		TreeColumn trclmnA = new TreeColumn(tree, SWT.NONE);
		trclmnA.setWidth(275);
		trclmnA.setText("课程名称");

		TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);
		treeColumn.setWidth(36);
		treeColumn.setText("学分");

		TreeColumn trlclmn_ls = new TreeColumn(tree, SWT.NONE);
		trlclmn_ls.setWidth(60);
		trlclmn_ls.setText("老师");

		TreeColumn trlclmn_pgtj = new TreeColumn(tree, SWT.NONE);
		trlclmn_pgtj.setWidth(275);
		trlclmn_pgtj.setText("先修课程");
		
		tree.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.item instanceof TreeItem) {
					TreeItem item = (TreeItem) e.item;
					boolean checked = item.getChecked();
					item.setGrayed(false);
					Stack<TreeItem> items = new Stack<>();
					items.push(item);
					while (!items.isEmpty()) {
						TreeItem item1 = items.pop();
						item1.setChecked(checked);
						item1.setGrayed(false);
						if (item1.getData() instanceof Course) {
							if (checked) {
								((Course) item1.getData()).layoutLable();
								((Course) item1.getData()).isChecked = true;
							} else {
								((Course) item1.getData()).disposeLable();
								((Course) item1.getData()).isChecked = false;
//								((Course) item1.getData()).isSelected = false;
							}
						}
						items.addAll(Arrays.asList(item1.getItems()));
					}
					
					TreeItem parent;
					TreeItem head = item;
					while ((parent = head.getParentItem()) != null) {
						if (checked) {
							parent.setChecked(true);
							parent.setGrayed(false);
							for (TreeItem item1 : parent.getItems()) {
								if (!item1.getChecked() || item1.getGrayed()) {
									parent.setGrayed(true);
									break;
								}
							}
						} else {
							parent.setGrayed(true);
							parent.setChecked(false);
							for (TreeItem item1 : parent.getItems()) {
								if (item1.getChecked()) {
									parent.setChecked(true);
									break;
								}
							}
						}
						head = parent;
					}
				}
			}
		});

		for (CourseRepo repo : CourseRepo.values()) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setData(repo);
			item.setText(repo.getName());
		}
		
		SashForm ver = new SashForm(scroll, SWT.HORIZONTAL | SWT.BORDER);
//		ver.setEnabled(false);

		for (String string : WEEK) {
			SashForm hor = new SashForm(ver, SWT.VERTICAL);
//			hor.setEnabled(false);

			Label lbl = new Label(hor, SWT.CENTER);
			lbl.setText(string);

			Composite composite = new Composite(hor, SWT.NONE);
			composite.setLayout(new FormLayout());

			for (int i = 0; i < 10; i++) {
				Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
				line.setData("static");
				
				FormData fd_line = new FormData();
				fd_line.left = new FormAttachment(0, 0);
				fd_line.right = new FormAttachment(100, 0);
				fd_line.top = new FormAttachment(i * 10, 0);
				line.setLayoutData(fd_line);
				if (string.equals("")) {
					Label num = new Label(composite, SWT.CENTER);
					FormData fd_num = new FormData();
					fd_num.left = new FormAttachment(0, 0);
					fd_num.right = new FormAttachment(100, 0);
					fd_num.top = new FormAttachment(i * 10 + 4, 0);
					num.setLayoutData(fd_num);
					num.setText(String.valueOf(i + 1));
				}
			}
			hor.setWeights(new int[] { 1, 40 });
			weekList.add(composite);
		}

		scroll.setContent(ver);

		ver.setWeights(new int[] { 2, 9, 9, 9, 9, 9, 9, 9 });
		
		for (Course course : selected) {
			course.layoutLable();
		}
	}
}
