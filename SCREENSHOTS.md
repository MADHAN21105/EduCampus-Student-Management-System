# 📸 Screenshot Guide for README

## How to Add Screenshots to Your README

Follow these steps to capture and add professional screenshots to your README:

### Step 1: Create Screenshots Folder

```bash
mkdir screenshots
```

### Step 2: Take Screenshots

Run your application and take screenshots of the following pages:

#### Required Screenshots:

1. **login.png** - Login page showing role selection
2. **student-dashboard.png** - Student dashboard main view
3. **teacher-dashboard.png** - Teacher dashboard main view
4. **admin-dashboard.png** - Admin dashboard main view
5. **attendance.png** - Attendance tracking interface
6. **leave-request.png** - Leave request form/list
7. **results.png** - Marks and results page
8. **mobile.png** - Mobile responsive view (resize browser to ~375px width)

### Step 3: Screenshot Tips

#### For Best Quality:

1. **Use Full HD Resolution** - 1920x1080 or higher
2. **Clean Browser** - Hide bookmarks bar, extensions
3. **Use Incognito Mode** - For clean screenshots without extensions
4. **Zoom Level** - Set browser zoom to 100%
5. **Sample Data** - Use realistic sample data, not "test test"

#### Recommended Tools:

- **Windows:** Snipping Tool (Win + Shift + S) or Greenshot
- **Mac:** Screenshot (Cmd + Shift + 4)
- **Linux:** GNOME Screenshot or Flameshot
- **Browser Extension:** Awesome Screenshot, Nimbus Screenshot

### Step 4: Optimize Images

Compress images to reduce file size:

```bash
# Using ImageMagick (if installed)
mogrify -resize 1920x1080 -quality 85 screenshots/*.png

# Or use online tools:
# - TinyPNG (https://tinypng.com/)
# - Squoosh (https://squoosh.app/)
```

### Step 5: Add to Git

```bash
git add screenshots/
git commit -m "Add screenshots to README"
git push
```

### Step 6: Verify README

Open your GitHub repository and check that all images display correctly.

---

## Screenshot Checklist

- [ ] Create `screenshots/` folder
- [ ] Take login.png
- [ ] Take student-dashboard.png
- [ ] Take teacher-dashboard.png
- [ ] Take admin-dashboard.png
- [ ] Take attendance.png
- [ ] Take leave-request.png
- [ ] Take results.png
- [ ] Take mobile.png
- [ ] Optimize all images
- [ ] Add to git
- [ ] Verify on GitHub

---

## Alternative: Use Placeholder Images

If you can't take screenshots right now, you can use placeholder images temporarily:

```markdown
![Login Page](https://via.placeholder.com/1200x600/2563eb/ffffff?text=Login+Page)
```

Replace with actual screenshots when available.

---

## Pro Tips

1. **Consistent Styling** - Use same sample data across screenshots
2. **Highlight Features** - Use browser dev tools to add temporary highlights
3. **Dark Mode** - Consider adding both light and dark mode screenshots
4. **Annotations** - Add arrows or text to highlight key features (use tools like Snagit)
5. **GIFs** - Consider adding animated GIFs for interactive features

---

## Example Screenshot Workflow

```bash
# 1. Start your application
mvn spring-boot:run

# 2. Open browser to http://localhost:8080

# 3. Take screenshots of each page

# 4. Save to screenshots/ folder with correct names

# 5. Optimize images
# (use online tool or ImageMagick)

# 6. Commit and push
git add screenshots/
git commit -m "Add application screenshots"
git push
```

---

Your README is now ready! Just add the screenshots and your project will look professional and complete. 🎉
