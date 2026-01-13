// Settings Management
document.addEventListener("DOMContentLoaded", () => {
  setupSettingsTabs()
})

function setupSettingsTabs() {
  const tabs = document.querySelectorAll(".settings-tab")
  const contents = document.querySelectorAll(".settings-content")

  tabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      const tabName = tab.dataset.tab

      // Update active tab
      tabs.forEach((t) => t.classList.remove("active"))
      tab.classList.add("active")

      // Update active content
      contents.forEach((content) => {
        content.classList.toggle("active", content.dataset.tab === tabName)
      })
    })
  })
}
