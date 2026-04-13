<template>
  <v-container fluid class="pa-4">
    <!-- Suchleiste -->
    <v-row class="mb-2" justify="center">
      <v-col cols="12" md="6">
        <v-text-field
          prepend-inner-icon="mdi-magnify"
          placeholder="Search..."
          variant="outlined"
          density="comfortable"
          hide-details
          clearable
          class="search-bar"
        />
      </v-col>
    </v-row>

    <!-- Filter-Tabs -->
    <v-row class="mb-6" justify="center">
      <v-col cols="12" md="8">
        <v-tabs
          v-model="activeFilter"
          align-tabs="center"
          color="primary"
          class="filter-tabs"
        >
          <v-tab value="all">All</v-tab>
          <v-tab value="favorites">Favorites</v-tab>
        </v-tabs>
      </v-col>
    </v-row>

    <!-- Sektionen mit Panels -->
    <div v-for="section in filteredSections" :key="section.id" class="mb-8">
      <div class="section-title text-h5 mb-3">
        {{ section.title }}
      </div>

      <v-row>
        <template v-for="n in section.count" :key="`${section.id}-${n}`">
          <v-col
            v-if="shouldShow(section.id, n)"
            cols="12"
            :sm="section.sm"
            :md="section.md"
            :class="{ 'panel-col-5': section.count === 5 }"
          >
            <v-card class="panel-card" elevation="2">
              <v-btn
                icon
                variant="text"
                class="favorite-btn"
                @click.stop="toggleFavorite(`${section.id}-${n}`)"
              >
                <v-icon
                  :color="isFavorite(`${section.id}-${n}`) ? 'red' : 'grey'"
                >
                  {{ isFavorite(`${section.id}-${n}`) ? 'mdi-heart' : 'mdi-heart-outline' }}
                </v-icon>
              </v-btn>

              <v-card-title class="panel-header">
                Panel {{ n }}
              </v-card-title>

              <v-card-text class="panel-content">
                <div class="image-placeholder">
                  <v-icon size="48" color="grey">mdi-image-outline</v-icon>
                  <div class="text-caption mt-2">Item {{ n }}</div>
                </div>
              </v-card-text>
            </v-card>
          </v-col>
        </template>
      </v-row>
    </div>
    
    <v-row v-if="filteredSections.length === 0" justify="center" class="mt-10">
      <v-col cols="auto" class="text-center text-grey">
        <v-icon size="64">mdi-heart-off-outline</v-icon>
        <div class="mt-2">No favorites marked yet</div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts" setup>
import { ref, reactive, computed } from 'vue';

const activeFilter = ref<'all' | 'favorites'>('all');

const sections = [
  { id: 'section1', title: 'Category 1', count: 3, sm: 4, md: 4 },
  { id: 'section2', title: 'Category 2', count: 1, sm: 12, md: 12 },
  { id: 'section3', title: 'Category 3', count: 5, sm: 6, md: 2 },
];

const favorites = reactive<Record<string, boolean>>({});

const toggleFavorite = (id: string) => {
  favorites[id] = !favorites[id];
};

const isFavorite = (id: string): boolean => !!favorites[id];

// Prüft, ob ein einzelnes Item angezeigt werden soll
const shouldShow = (sectionId: string, n: number): boolean => {
  if (activeFilter.value === 'all') return true;
  return isFavorite(`${sectionId}-${n}`);
};

// Filtert ganze Sektionen heraus, wenn sie im Favoriten-Modus leer wären
const filteredSections = computed(() => {
  return sections.filter(section => {
    for (let n = 1; n <= section.count; n++) {
      if (shouldShow(section.id, n)) return true;
    }
    return false;
  });
});
</script>

<style scoped>
.filter-tabs {
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}

.section-title {
  border-bottom: 2px solid rgb(var(--v-theme-primary));
  padding-bottom: 8px;
}

.panel-card {
  height: 250px;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: transform 0.2s ease;
}

.panel-card:hover {
  transform: translateY(-4px);
}

.panel-header {
  font-size: 1rem;
  font-weight: 500;
}

.panel-content {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 150px;
  border: 2px dashed rgba(0, 0, 0, 0.12);
  border-radius: 8px;
}

.favorite-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 10;
}

@media (min-width: 960px) {
  .panel-col-5 {
    flex: 0 0 20% !important;
    max-width: 20% !important;
  }
}
</style>
