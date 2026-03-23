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
                ></v-text-field>
            </v-col>
        </v-row>

        <!-- Filter Tabs -->
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

        <!-- Section 1: 3 Panels nebeneinander -->
        <div v-if="activeFilter === 'all'">
            <div class="section-title text-h5 mb-3">Category 1 - Three Panels</div>
            <v-row class="mb-6">
                <v-col cols="12" sm="4" v-for="n in 3" :key="'section1-' + n">
                    <v-card class="panel-card" elevation="2">
                        <v-card-title class="panel-header">
                            Panel {{ n }}
                        </v-card-title>
                        <v-card-text class="panel-content">
                            <div class="image-placeholder">
                                <v-icon size="48" color="grey">mdi-image-outline</v-icon>
                                <div class="text-caption mt-2">Image Placeholder</div>
                            </div>
                        </v-card-text>
                    </v-card>
                </v-col>
            </v-row>
        </div>

        <!-- Section 2: 1 Panel -->
        <div v-if="activeFilter === 'all'">
            <div class="section-title text-h5 mb-3">Category 2 - One Panel</div>
            <v-row class="mb-6">
                <v-col cols="12">
                    <v-card class="panel-card" elevation="2">
                        <v-card-title class="panel-header">
                            Panel 1
                        </v-card-title>
                        <v-card-text class="panel-content">
                            <div class="image-placeholder">
                                <v-icon size="48" color="grey">mdi-image-outline</v-icon>
                                <div class="text-caption mt-2">Image Placeholder</div>
                            </div>
                        </v-card-text>
                    </v-card>
                </v-col>
            </v-row>
        </div>
        
        <!-- Section 3: 5 Panels -->
        <div v-if="activeFilter === 'all'">
            <div class="section-title text-h5 mb-3">Category 3 - Five Panels</div>
            <v-row>
                <v-col cols="12" sm="6" md="2.4" v-for="n in 5" :key="'section3-' + n" class="panel-col-5">
                    <v-card class="panel-card" elevation="2">
                        <v-card-title class="panel-header">
                            Panel {{ n }}
                        </v-card-title>
                        <v-card-text class="panel-content">
                            <div class="image-placeholder">
                                <v-icon size="48" color="grey">mdi-image-outline</v-icon>
                                <div class="text-caption mt-2">Image Placeholder</div>
                            </div>
                        </v-card-text>
                    </v-card>
                </v-col>
            </v-row>
        </div>
    </v-container>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
const activeFilter = ref('all');
</script>

<style scoped>
    .filter-tabs {
        border-bottom: 1px solid rgb(var(--v-theme-divider));
    }

    .section-title {
        color: rgb(var(--v-theme-titleText));
        border-bottom: 2px solid rgb(var(--v-theme-primary));
        padding-bottom: 8px;
    }

    .panel-card {
        height: 250px;
        display: flex;
        flex-direction: column;
        background-color: rgb(var(--v-theme-card));
        transition: transform 0.2s;
    }

    .panel-card:hover {
        transform: translateY(-4px);
    }

    .panel-header {
        background-color: rgb(var(--v-theme-cardHeader));
        font-size: 1rem;
        font-weight: 500;
        flex-shrink: 0;
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
        border: 2px dashed rgb(var(--v-theme-divider));
        border-radius: 8px;
        background-color: rgb(var(--v-theme-detailsCard));
    }

    /* 5 Panels in einer Reihe */
    @media (min-width: 960px) {
        .panel-col-5 {
            flex: 0 0 20%;
            max-width: 20%;
        }
    }
</style>
