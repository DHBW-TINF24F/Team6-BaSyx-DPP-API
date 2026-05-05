<template>
  <v-container fluid class="pa-4">

    <!-- Suchfeld für Filterung der AAS -->
    <v-row class="mb-6" justify="center">
      <v-col cols="12" md="6">
        <v-text-field
          v-model="search"
          prepend-inner-icon="mdi-magnify"
          placeholder="AAS search..."
          variant="outlined"
          density="comfortable"
          clearable
        />
      </v-col>
    </v-row>

    <!-- Titel der Liste -->
    <div class="section-title text-h5 mb-3">
      Available AAS
    </div>

    <!-- Liste der AAS -->
    <v-card elevation="2">
      <v-list lines="two">

        <!-- Dynamische Liste aller AAS -->
        <v-list-item
          v-for="aas in filteredAas"
          :key="aas.id"
          :title="aas.name"
          :subtitle="aas.description"
          
          @click="goToAas (aas.id)" class="aas-item">
        
          <!-- Icon links -->
          <template #prepend>
            <v-icon color="primary">mdi-cube-outline</v-icon>
          </template>

          <!-- Pfeil rechts -->
          <template #append>
            <v-icon>mdi-chevron-right</v-icon>
          </template>

        </v-list-item>

      </v-list>
    </v-card>

  </v-container>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

/* Router für Navigation */
const router = useRouter()

/* Suchfeld */
const search = ref('')

/* Beispiel AAS Daten (später API ersetzen) */
const aasList = ref([
  { id: 1, name: 'AAS 1', description: 'AAS 1 Description' },
  { id: 2, name: 'AAS 2', description: 'AAS 2 Description' },
  { id: 3, name: 'AAS 3', description: 'AAS 3 Description' },
])

/* Filterlogik für Suche */
const filteredAas = computed(() => {
  if (!search.value) return aasList.value

  return aasList.value.filter(a =>
    a.name.toLowerCase().includes(search.value.toLowerCase())
  )
})

/* Navigation zur AAS Seite */
function goToAas(id: number) {
  router.push(`/aas/${id}`)
}
</script>

<style scoped>
.section-title {
  color: rgb(var(--v-theme-titleText));
  border-bottom: 2px solid rgb(var(--v-theme-primary));
  padding-bottom: 8px;
}

.aas-item {
  cursor: pointer;
}

.aas-item:hover {
  background-color: rgba(var(--v-theme-primary), 0.08);
}
</style>
