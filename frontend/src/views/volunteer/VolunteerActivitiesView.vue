<template>
  <div>
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :items="activities"
        :search="search"
        disable-pagination
        :hide-default-footer="true"
        :mobile-breakpoint="0"
        data-cy="volunteerActivitiesTable"
      >
        <template v-slot:top>
          <v-card-title>
            <v-text-field
              v-model="search"
              append-icon="search"
              label="Search"
              class="mx-2"
            />
            <v-spacer />
          </v-card-title>
        </template>
        <template v-slot:[`item.themes`]="{ item }">
          <v-chip v-for="theme in item.themes" v-bind:key="theme.id">
            {{ theme.completeName }}
          </v-chip>
        </template>
        <template v-slot:[`item.action`]="{ item }">
          <v-tooltip v-if="item.state === 'APPROVED'" bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                color="red"
                v-on="on"
                data-cy="reportButton"
                @click="reportActivity(item)"
                >warning
              </v-icon>
            </template>
            <span>Report Activity</span>
          </v-tooltip>
          <!-- new button -->
          <v-tooltip v-if="isAssessable(item)" bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                color="blue"
                v-on="on"
                data-cy="assessmentButton"
                @click="newAssessment(item)"
                >mdi-square-edit-outline
              </v-icon>
            </template>
            <span>Write Assessment</span>
          </v-tooltip>
        </template>
      </v-data-table>
      <assessment-dialog
        v-if="currentActivity && assessmentDialog"
        v-model="assessmentDialog"
        :activity="currentActivity"
        v-on:apply="onCreateAssessment"
        v-on:close-assessment-dialog="onCloseAssessmentDialog"
      />
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Activity from '@/models/activity/Activity';
import AssessmentDialog from '@/views/volunteer/AssessmentDialog.vue';
import { show } from 'cli-cursor';
import Participation from '@/models/participation/Participation';
import Assessment from '@/models/assessment/Assessment';

@Component({
  components: {
    'assessment-dialog': AssessmentDialog,
  },
  methods: { show },
})
export default class VolunteerActivitiesView extends Vue {
  activities: Activity[] = [];
  participations: Participation[] = [];
  assessments: Assessment[] = [];
  search: string = '';

  currentActivity: Activity | null = null;
  assessmentDialog: boolean = false;

  headers: object = [
    {
      text: 'Name',
      value: 'name',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Region',
      value: 'region',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Participants',
      value: 'participantsNumberLimit',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Themes',
      value: 'themes',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Description',
      value: 'description',
      align: 'left',
      width: '30%',
    },
    {
      text: 'State',
      value: 'state',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Start Date',
      value: 'formattedStartingDate',
      align: 'left',
      width: '5%',
    },
    {
      text: 'End Date',
      value: 'formattedEndingDate',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Application Deadline',
      value: 'formattedApplicationDeadline',
      align: 'left',
      width: '5%',
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      sortable: false,
      width: '5%',
    },
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.activities = await RemoteServices.getActivities();
      this.participations = await RemoteServices.getVolunteerParticipation();
      this.assessments = await RemoteServices.getVolunteerAssessments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  //asdasdasd
  isAssessable(activity: Activity) {
    // check if activity has ended
    if (Date.now() < new Date(activity.endingDate).getTime()) return false;

    // check if user has already assessed this institution
    if (
      this.assessments.find((a) => a.institutionId === activity.institution.id)
    )
      return false;

    // check if user has participated in this activity
    if (!this.participations.find((p) => p.activityId === activity.id))
      return false;

    return true;
  }

  async reportActivity(activity: Activity) {
    if (activity.id !== null) {
      try {
        const result = await RemoteServices.reportActivity(
          this.$store.getters.getUser.id,
          activity.id,
        );
        this.activities = this.activities.filter((a) => a.id !== activity.id);
        this.activities.unshift(result);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  async newAssessment(activity: Activity) {
    this.currentActivity = activity;
    this.assessmentDialog = true;
  }

  onCreateAssessment(assessment: Assessment) {
    this.assessments.push(assessment);
    this.currentActivity = null;
    this.assessmentDialog = false;
  }

  onCloseAssessmentDialog() {
    this.currentActivity = null;
    this.assessmentDialog = false;
  }
}
</script>

<style lang="scss" scoped></style>
